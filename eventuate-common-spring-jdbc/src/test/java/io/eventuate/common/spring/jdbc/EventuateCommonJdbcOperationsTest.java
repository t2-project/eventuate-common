package io.eventuate.common.spring.jdbc;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.*;
import io.eventuate.common.jdbc.sqldialect.EventuateSqlDialect;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.common.jdbc.tests.AbstractEventuateCommonJdbcOperationsTest;
import io.eventuate.common.spring.id.IdGeneratorConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

@SpringBootTest(classes = EventuateCommonJdbcOperationsTest.Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EventuateCommonJdbcOperationsTest extends AbstractEventuateCommonJdbcOperationsTest {

  @Configuration
  @EnableAutoConfiguration
  @Import({EventuateCommonJdbcOperationsConfiguration.class, IdGeneratorConfiguration.class})
  public static class Config {
  }

  @Value("${spring.datasource.driver-class-name}")
  private String driver;

  @Autowired
  private EventuateCommonJdbcOperations eventuateCommonJdbcOperations;

  @Autowired
  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;

  @Autowired
  private EventuateTransactionTemplate eventuateTransactionTemplate;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private SqlDialectSelector sqlDialectSelector;

  @Autowired
  private IdGenerator idGenerator;

  @Test(expected = EventuateDuplicateKeyException.class)
  @Override
  public void testEventuateDuplicateKeyException() {
    super.testEventuateDuplicateKeyException();
  }

  @Test
  @Override
  public void testInsertIntoEventsTable() throws SQLException {
    super.testInsertIntoEventsTable();
  }

  @Test
  @Override
  public void testInsertIntoMessageTable() throws SQLException {
    super.testInsertIntoMessageTable();
  }

  @Test
  public void testJsonColumnToStringConversion() {
    EventuateSchema eventuateSchema = new EventuateSchema();

    String payloadData = generateId();
    String rawPayload = "\"" + payloadData + "\"";

    String messageId = eventuateCommonJdbcOperations
            .insertIntoMessageTable(idGenerator, rawPayload, "", "0", Collections.emptyMap(), eventuateSchema);

    IdColumnAndValue idColumnAndValue = messageIdToRowId(messageId);

    SqlRowSet sqlRowSet = jdbcTemplate
            .queryForRowSet(String.format("select payload from %s where %s = ?",
                    eventuateSchema.qualifyTable("message"), idColumnAndValue.getColumn()), idColumnAndValue.getValue());

    sqlRowSet.next();

    Object payload = sqlRowSet.getObject("payload");

    EventuateSqlDialect eventuateSqlDialect = sqlDialectSelector.getDialect(driver);

    String payloadString = eventuateSqlDialect.jsonColumnToString(payload,
            eventuateSchema, "message", "payload", eventuateJdbcStatementExecutor);

    Assert.assertTrue(payloadString.contains(payloadData));
  }

  @Override
  protected EventuateCommonJdbcOperations getEventuateCommonJdbcOperations() {
    return eventuateCommonJdbcOperations;
  }

  @Override
  protected EventuateTransactionTemplate getEventuateTransactionTemplate() {
    return eventuateTransactionTemplate;
  }

  @Override
  protected IdGenerator getIdGenerator() {
    return idGenerator;
  }

  @Override
  protected DataSource getDataSource() {
    return dataSource;
  }

  @Override
  protected EventuateJdbcStatementExecutor getEventuateJdbcStatementExecutor() {
    return eventuateJdbcStatementExecutor;
  }
}
