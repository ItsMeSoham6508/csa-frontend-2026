package org.mp.frontend26;

import java.sql.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.mp.frontend26.dto.*;

public class MpDatabase {

    private Connection connection;
    private boolean connected = false;

    public MpDatabase() {}

    // ================== CONNECTION ==================

    public void connect() throws SQLException {
        if (connected) return;

        try {
            Properties props = new Properties();
            try (InputStream in = MpDatabase.class.getResourceAsStream("/db.properties")) {
                if (in == null) throw new RuntimeException("db.properties not found");
                props.load(in);
            }

            connection = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.pass")
            );

            connected = true;

        } catch (Exception e) {
            connected = false;
            throw new SQLException("DB connection failed", e);
        }
    }

    public void close() {
        if (!connected) return;
        try {
            connection.close();
        } catch (SQLException ignored) {}
        connected = false;
    }

    private void check() throws SQLException {
        if (!connected || connection == null || connection.isClosed()) {
            throw new SQLException("Database not connected. Call connect()");
        }
    }

    // ================== MODULES ==================

    public List<AWSModule> getAllModules() throws SQLException {
        check();
        List<AWSModule> list = new ArrayList<>();

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM modules")) {

            while (rs.next()) {
                AWSModule m = mapModule(rs);
                list.add(m);
            }
        }
        return list;
    }

    public AWSModule getModuleById(int id) throws SQLException {
        check();
        String sql = "SELECT * FROM modules WHERE module_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapModule(rs) : null;
        }
    }

    public int createModule(AWSModule m) throws SQLException {
        check();
        String sql = """
            INSERT INTO modules
            (module_title, module_summary, learning_outcomes, image_uri,
             sales_pitch, time_requirement, difficulty_level)
            VALUES (?,?,?,?,?,?,?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillModule(ps, m);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    public void updateModule(AWSModule m) throws SQLException {
        check();
        String sql = """
            UPDATE modules SET
            module_title=?, module_summary=?, learning_outcomes=?, image_uri=?,
            sales_pitch=?, time_requirement=?, difficulty_level=?
            WHERE module_id=?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillModule(ps, m);
            ps.setInt(8, m.moduleId);
            ps.executeUpdate();
        }
    }

    public void deleteModule(int id) throws SQLException {
        check();
        try (PreparedStatement ps =
                     connection.prepareStatement("DELETE FROM modules WHERE module_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ================== EXAMPLES (FK → modules) ==================

    public List<Example> getExamplesByModuleId(int moduleId) throws SQLException {
        check();
        List<Example> list = new ArrayList<>();

        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT * FROM examples WHERE module_id=?")) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapExample(rs));
        }
        return list;
    }

    public Example getExampleById(int id) throws SQLException {
        check();
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT * FROM examples WHERE use_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapExample(rs) : null;
        }
    }

    public int createExample(Example e) throws SQLException {
        check();
        String sql = """
            INSERT INTO examples
            (module_id, use_title, use_description, industry_example)
            VALUES (?,?,?,?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.moduleId); // FK enforced by DB
            ps.setString(2, e.useTitle);
            ps.setString(3, e.useDescription);
            ps.setString(4, e.industryExample);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    public void updateExample(Example e) throws SQLException {
        check();
        String sql = """
            UPDATE examples SET
            module_id=?, use_title=?, use_description=?, industry_example=?
            WHERE use_id=?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, e.moduleId);
            ps.setString(2, e.useTitle);
            ps.setString(3, e.useDescription);
            ps.setString(4, e.industryExample);
            ps.setInt(5, e.useId);
            ps.executeUpdate();
        }
    }

    public void deleteExample(int id) throws SQLException {
        check();
        try (PreparedStatement ps =
                     connection.prepareStatement("DELETE FROM examples WHERE use_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ================== KNOWLEDGE CHECKS (FK + CASCADE) ==================

    public List<KnowledgeCheck> getKnowledgeChecksByModuleId(int moduleId) throws SQLException {
        check();
        List<KnowledgeCheck> list = new ArrayList<>();

        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT * FROM knowledge_checks WHERE module_id=?")) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapKnowledgeCheck(rs));
        }
        return list;
    }

    public int createKnowledgeCheck(KnowledgeCheck k) throws SQLException {
        check();
        String sql = """
            INSERT INTO knowledge_checks
            (module_id, question, option_a, option_b, option_c, option_d,
             correct_answer, explanation)
            VALUES (?,?,?,?,?,?,?,?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillKnowledgeCheck(ps, k);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    public void updateKnowledgeCheck(KnowledgeCheck k) throws SQLException {
        check();
        String sql = """
            UPDATE knowledge_checks SET
            module_id=?, question=?, option_a=?, option_b=?, option_c=?,
            option_d=?, correct_answer=?, explanation=?
            WHERE id=?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillKnowledgeCheck(ps, k);
            ps.setInt(9, k.id);
            ps.executeUpdate();
        }
    }

    public void deleteKnowledgeCheck(int id) throws SQLException {
        check();
        try (PreparedStatement ps =
                     connection.prepareStatement("DELETE FROM knowledge_checks WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ================== AWS SERVICE ==================

    public List<AWSService> getAllServices() throws SQLException {
        check();
        List<AWSService> list = new ArrayList<>();

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM aws_service")) {
            while (rs.next()) list.add(mapService(rs));
        }
        return list;
    }

    public int createService(AWSService s) throws SQLException {
        check();
        String sql = """
            INSERT INTO aws_service
            (service_name, category, service_description,
             free_tier_eligibility, documentation_url)
            VALUES (?,?,?,?,?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.serviceName);
            ps.setString(2, s.category);
            ps.setString(3, s.serviceDescription);
            ps.setBoolean(4, s.freeTierEligibility);
            ps.setString(5, s.documentationUrl);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    public void updateService(AWSService s) throws SQLException {
        check();
        String sql = """
            UPDATE aws_service SET
            service_name=?, category=?, service_description=?,
            free_tier_eligibility=?, documentation_url=?
            WHERE id=?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s.serviceName);
            ps.setString(2, s.category);
            ps.setString(3, s.serviceDescription);
            ps.setBoolean(4, s.freeTierEligibility);
            ps.setString(5, s.documentationUrl);
            ps.setInt(6, s.id);
            ps.executeUpdate();
        }
    }

    public void deleteService(int id) throws SQLException {
        check();
        try (PreparedStatement ps =
                     connection.prepareStatement("DELETE FROM aws_service WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ================== MAPPERS ==================

    private AWSModule mapModule(ResultSet rs) throws SQLException {
        AWSModule m = new AWSModule();
        m.moduleId = rs.getInt("module_id");
        m.moduleTitle = rs.getString("module_title");
        m.moduleSummary = rs.getString("module_summary");
        m.learningOutcomes = rs.getString("learning_outcomes");
        m.imageUri = rs.getString("image_uri");
        m.salesPitch = rs.getString("sales_pitch");
        m.timeRequirement = rs.getFloat("time_requirement");
        m.difficultyLevel = rs.getDouble("difficulty_level");
        return m;
    }

    private void fillModule(PreparedStatement ps, AWSModule m) throws SQLException {
        ps.setString(1, m.moduleTitle);
        ps.setString(2, m.moduleSummary);
        ps.setString(3, m.learningOutcomes);
        ps.setString(4, m.imageUri);
        ps.setString(5, m.salesPitch);
        ps.setFloat(6, m.timeRequirement);
        ps.setDouble(7, m.difficultyLevel);
    }

    private Example mapExample(ResultSet rs) throws SQLException {
        Example e = new Example();
        e.useId = rs.getInt("use_id");
        e.moduleId = rs.getInt("module_id");
        e.useTitle = rs.getString("use_title");
        e.useDescription = rs.getString("use_description");
        e.industryExample = rs.getString("industry_example");
        return e;
    }

    private KnowledgeCheck mapKnowledgeCheck(ResultSet rs) throws SQLException {
        KnowledgeCheck k = new KnowledgeCheck();
        k.id = rs.getInt("id");
        k.moduleId = rs.getInt("module_id");
        k.question = rs.getString("question");
        k.optionA = rs.getString("option_a");
        k.optionB = rs.getString("option_b");
        k.optionC = rs.getString("option_c");
        k.optionD = rs.getString("option_d");
        k.correctAnswer = rs.getString("correct_answer");
        k.explanation = rs.getString("explanation");
        return k;
    }

    private void fillKnowledgeCheck(PreparedStatement ps, KnowledgeCheck k) throws SQLException {
        ps.setInt(1, k.moduleId);
        ps.setString(2, k.question);
        ps.setString(3, k.optionA);
        ps.setString(4, k.optionB);
        ps.setString(5, k.optionC);
        ps.setString(6, k.optionD);
        ps.setString(7, k.correctAnswer);
        ps.setString(8, k.explanation);
    }

    private AWSService mapService(ResultSet rs) throws SQLException {
        AWSService s = new AWSService();
        s.id = rs.getInt("id");
        s.serviceName = rs.getString("service_name");
        s.category = rs.getString("category");
        s.serviceDescription = rs.getString("service_description");
        s.freeTierEligibility = rs.getBoolean("free_tier_eligibility");
        s.documentationUrl = rs.getString("documentation_url");
        return s;
    }
}
