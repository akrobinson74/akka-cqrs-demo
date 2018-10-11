CREATE TABLE orders(
  ID                  BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  CREATED_AT          TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT          TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  MSG_SEQ_NR          BIGINT(20)              NOT NULL,
  USER_ID             TEXT                    NOT NULL,
  ORDER_ID            TEXT                    NOT NULL,
  PAYMENT_IDENTIFIER  TEXT                    NOT NULL,
  ORDER_ITEM_IDS      TEXT                    NOT NULL,
  SOURCE              TEXT                    NOT NULL,
  STATUS              TEXT                    NOT NULL
)