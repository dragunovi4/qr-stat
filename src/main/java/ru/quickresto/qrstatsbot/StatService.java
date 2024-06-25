package ru.quickresto.qrstatsbot;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;

@Service
public class StatService {
    private int single, franch, daught, evotor, total = 0;
    private int daught2, single2, franch2, evotor2, total2 = 0;
    private String resultFr, resultDr, resultSin, resultEvo, resultTotal;
    private String intro = "Текущая статистика по облакам: ";

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
        franch = 0;
        daught = 0;
        evotor = 0;
        total = 0;
        daught2 = 0;
        single2 = 0;
        franch2 = 0;
        evotor2 = 0;
        total2 = 0;
    }

    public String collectionStatistics() {
        clearStatistics();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement ps1 = conn.prepareStatement("select key, value from sys_extras where value != 'customers'");
            ResultSet rs1 = ps1.executeQuery();

            while (rs1.next()) {
                String key = rs1.getString("key");
                int value = Integer.parseInt(rs1.getString("value"));
                if (key.contains("daught")) {
                    daught = value;
                } else if (key.contains("single")) {
                    single = value;
                } else if (key.contains("franch")) {
                    franch = value;
                } else {
                    evotor = value;
                }
            }

            total = franch + daught + single + evotor;

            PreparedStatement ps2 = conn.prepareStatement("select count(*), p.name from customer c left join profile p on c.profile_id=p.id where c.name not like '%test%' and c.state = 'BOUND' group by p.name");
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                String name = rs2.getString("name");
                int count = rs2.getInt("count");
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

            total2 = franch2 + daught2 + single2 + evotor2;

            resultFr = differenceCalculation("Франшиз (родители)", franch, franch2 );
            resultDr = differenceCalculation("Франшиз (дочки)", daught, daught2 );
            resultSin = differenceCalculation("Синглов", single, single2 );
            resultEvo = differenceCalculation("Эвоторов", evotor, evotor2 );
            resultTotal = differenceCalculation("Всего", total, total2 );

            System.out.println(resultFr + "\n" + resultDr + "\n" + resultSin + "\n" + resultEvo + "\n" + resultTotal);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (isFirstDayOfMonth()) {
            updateDatabase();
        }

        return getAdditionalInfo();
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
        return intro + "\n" +
                resultFr + "\n" +
                resultDr + "\n" +
                resultSin + "\n" +
                resultEvo + "\n" +
                resultTotal;
    }
}
