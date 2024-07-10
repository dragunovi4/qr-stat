package ru.quickresto.qrstatsbot;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Calendar;

@Service
public class StatService {
    private int single, franch, daught, evotor, total, trial = 0;
    private int daught2, single2, franch2, evotor2, total2 = 0;
    private int daught3, single3, franch3, evotor3, total3 = 0;
    private String resultFr, resultDr, resultSin, resultEvo, resultTotal;
    private String intro = "Текущая статистика по облакам";

    static String url = "jdbc:postgresql://localhost:5432/janitordb";
    static String user = "janitor";
    static String password = "janitor";

    private static String differenceCalculation(String label, int oldValue, int newValue) {
        int difference = newValue - oldValue;
        double percentDifference = ((double) difference / oldValue) * 100;
        DecimalFormat df = new DecimalFormat("0.00");
        return label + ": " + newValue + " (" + difference + " / " + df.format(percentDifference) + " %)";
    }

    private void clearStatistics() {
        single = 0;
        trial = 0;
        franch = 0;
        daught = 0;
        evotor = 0;
        total = 0;
        daught2 = 0;
        single2 = 0;
        franch2 = 0;
        evotor2 = 0;
        total2 = 0;
        franch3 = 0;
        daught3 = 0;
        single3 = 0;
        evotor3 = 0;
        total3 = 0;
    }

    public String collectionStatistics() {
        clearStatistics();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            for (ResultSet rs = executeSql(conn, "SELECT key, value FROM sys_extras WHERE key IN ('trial', 'single', 'evotor', 'franch', 'daught');"); rs.next(); ) {
                String key = rs.getString("key");
                int value = Integer.parseInt(rs.getString("value"));


                if (key.contains("daught")) {
                    daught = value;
                } else if (key.contains("single")) {
                    single = value;
                } else if (key.contains("franch")) {
                    franch = value;
                } else if (key.contains("trial")) {
                    trial = value;
                }else {
                    evotor = value;
                }
            }

            total = franch + daught + single + evotor;

            for (ResultSet rs = executeSql(conn, "select count(*), p.name from customer c left join profile p on c.profile_id=p.id where c.name not like '%test%' and c.state = 'BOUND' group by p.name"); rs.next(); ) {
                String name = rs.getString("name");
                int count = Integer.parseInt(rs.getString("count"));

                if (name == null) {
                    daught2 += count;
                } else if (name.contains("single")) {
                    single2 += count;
                } else if (name.contains("franch")) {
                    franch2 += count;
                } else if (name.contains("evotor")) {
                    evotor2 += count;
                } else {
                    System.out.println("Необработанные облака: " + name);
                }
            }

            total2 = franch2 + daught2 + single2 + evotor2 - total3;

            for (ResultSet rs = executeSql(conn, "select count(*), p.name, ex.value from customer c left join extras ex on ex.owner_id=c.id left join profile p on c.profile_id=p.id where c.name not like '%test%' and c.state = 'BOUND' and ex.key = 'tariff' and ex.value = 'trial' group by p.name, ex.value ;"); rs.next(); ) {
                String name = rs.getString("name");
                int count = rs.getInt("count");

                if (name == null) {
                    daught3 += count;
                } else if (name.contains("single")) {
                    single3 += count;
                } else if (name.contains("franch")) {
                    franch3 += count;
                } else if (name.contains("evotor")) {
                    evotor3 += count;
                } else {
                    System.out.println("Необработанные облака: " + name);
                }
            }


            total3 = daught3 + single3 + franch3 + evotor3;

            resultFr = differenceCalculation("Франшиз (родители)", franch, (franch2 - franch3));
            resultDr = differenceCalculation("Франшиз (дочки)", daught, (daught2 - daught3));
            resultSin = differenceCalculation("Синглов", single, (single2 - single3));
            resultEvo = differenceCalculation("Эвоторов", evotor, (evotor2 - evotor3));
            resultTotal = differenceCalculation("Всего", total, (total2 - total3));

            System.out.println(resultFr + "\n" + resultDr + "\n" + resultSin + "\n" + resultEvo + "\n" + resultTotal);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (isFirstDayOfMonth()) {
            updateDatabase();
        }

        return getAdditionalInfo();
    }

    private ResultSet executeSql(Connection conn, String sql) throws SQLException {
        return conn.prepareStatement(sql)
            .executeQuery();
    }

    private boolean isFirstDayOfMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        return today.equals(firstDayOfMonth);
    }

    private void updateDatabase() {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String updateQuery = "UPDATE sys_extras SET value = ? WHERE key = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);

            updateFieldValue(preparedStatement, "daught", daught2);
            updateFieldValue(preparedStatement, "single", single2);
            updateFieldValue(preparedStatement, "franch", franch2);
            updateFieldValue(preparedStatement, "evotor", evotor2);
            updateFieldValue(preparedStatement, "trial", total3);

            System.out.println("База данных успешно обновлена.");
        } catch (SQLException e) {
            System.out.println("SQL exception: " + e.getMessage());
        }
    }

    private void updateFieldValue(PreparedStatement preparedStatement, String key, int value) throws SQLException {
        preparedStatement.setInt(1, value);
        preparedStatement.setString(2, key);
        preparedStatement.executeUpdate();
    }

    private String getAdditionalInfo() {
        return intro + (" (относительно " + getLastMonthLastDay() + ")") + "\n" +
                resultFr + "\n" +
                resultDr + "\n" +
                resultSin + "\n" +
                resultEvo + "\n" +
                resultTotal + "\n" +
        "Всего триальных облаков: " + total3;
    }

    private String getLastMonthLastDay() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        return cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.YEAR);
    }
}
