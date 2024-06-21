package com.example.qrstatst;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class StatService {
    static int single, franch, daught, evotor, total = 0;
    static int daught2, single2, franch2, evotor2, total2 = 0;
    static String resultFr, resultDr, resultSin, resultEvo, resultTotal;
    String intro = "Текущая статистика по облакам: ";

    static String url = "jdbc:postgresql://localhost:5432/janitor";
    static String user = "janitor";
    static String password = "janitor";

    public static void collectionStatistics() {
        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            conn = DriverManager.getConnection(url, user, password);
            ps1 = conn.prepareStatement("select key, value from sys_extras where value != 'customers'");
            rs1 = ps1.executeQuery();

            while (rs1.next()) {
                String key = rs1.getString("key");
                int value = Integer.valueOf(rs1.getString("value"));
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

            if (rs2 != null) {
                rs2.close();
            }

            ps2 = conn.prepareStatement("select count(*), p.name from customer c left join profile p on c.profile_id=p.id where c.name not like '%test%' group by p.name");
            rs2 = ps2.executeQuery();

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
        } finally {
            try {
                if (rs1 != null) {
                    rs1.close();
                }
                if (ps1 != null) {
                    ps1.close();
                }
                if (rs2 != null) {
                    rs2.close();
                }
                if (ps2 != null) {
                    ps2.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        if (today.equals(firstDayOfMonth)) {
            updateDatabase();
        } else {
            System.out.println("Сегодня не первый день месяца.");
        }
    }
    private static String differenceCalculation(String label, int oldValue, int newValue) {
        int difference = newValue - oldValue;
        double percentDifference = ((double) difference / oldValue) * 100;
        DecimalFormat df = new DecimalFormat("0.00");
        return label + ": " + newValue + " (" + difference + " / " + df.format(percentDifference) + " %)";

    }
    private static void updateDatabase() {
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

    private static void updateFieldValue(PreparedStatement preparedStatement, String key, int value) throws SQLException {
        preparedStatement.setInt(1, value);
        preparedStatement.setString(2, key);
        preparedStatement.executeUpdate();
    }

    public String getAdditionalInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append(intro).append("\n")
                .append(resultFr).append("\n")
                .append(resultDr).append("\n")
                .append(resultSin).append("\n")
                .append(resultEvo).append("\n")
                .append(resultTotal);
        return builder.toString();
    }
}
