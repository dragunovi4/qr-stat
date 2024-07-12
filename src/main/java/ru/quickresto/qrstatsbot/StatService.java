package ru.quickresto.qrstatsbot;

import freemarker.template.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

@Service
public class StatService {

    @Value("${database.url}")
    private String url;

    @Value("${database.user}")
    private String user;

    @Value("${database.password}")
    private String password;

    public static String readFileToString(String path) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(path);
        return resource.getContentAsString(Charset.defaultCharset());
    }

    public String collectionStatistics() throws IOException, TemplateException {
        String sql = readFileToString("sql/layer-stat.sql");

        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            for (ResultSet rs = executeSql(conn, sql); rs.next();) {
                stats.put(rs.getString("profile"), new LayerStat(
                        rs.getString("profile"),
                        rs.getInt("diff"),
                        rs.getInt("trial"),
                        rs.getBigDecimal("percent"),
                        rs.getInt("current")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        stats.put("total", new LayerStat(
                "total",
                0,
                0,
                BigDecimal.ONE, 0));

        stats.put("date", getLastMonthLastDay());

        Configuration cfg = new Configuration();
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates/");

        Template template = cfg.getTemplate("layer-stat.ftl");
        StringWriter writer = new StringWriter();
        template.process(stats, writer);

        if (isFirstDayOfMonth()) {
            updateDatabase();
        }

        return writer.toString();
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

//            updateFieldValue(preparedStatement, "daught", daught2);
//            updateFieldValue(preparedStatement, "single", single2);
//            updateFieldValue(preparedStatement, "franch", franch2);
//            updateFieldValue(preparedStatement, "evotor", evotor2);
//            updateFieldValue(preparedStatement, "trial", total3);

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

    private String getLastMonthLastDay() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        return cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.YEAR);
    }
}
