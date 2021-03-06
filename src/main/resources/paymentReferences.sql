CREATE TABLE paymentReferences(
  ID                  BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  CREATED_AT          TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT          TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  MSG_SEQ_NR          BIGINT(20)              NOT NULL,
  AMOUNT              DECIMAL                 NOT NULL,
  CURRENCY            TEXT                    NOT NULL,
  EXECUTION_TIME      TIMESTAMP               NOT NULL,
  PAYMENT_IDENTIFIER  TEXT                    NOT NULL
)