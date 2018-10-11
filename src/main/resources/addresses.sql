CREATE TABLE addresses(
  ID            BIGINT(20) PRIMARY KEY  NOT NULL AUTO_INCREMENT,
  CREATED_AT    TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT    TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  MSG_SEQ_NR    BIGINT(20)              NOT NULL,
  ADDRESS_LINES TEXT                    NOT NULL DEFAULT "",
  CITY          TEXT                    NOT NULL,
  COUNTRY       TEXT                    NOT NULL,
  HOUSE_NUMBER  TEXT                    NOT NULL,
  REGION        TEXT,
  STATE         TEXT,
  STATE_CODE    TEXT,
  STREET        TEXT                    NOT NULL,
  USER_ID       TEXT                    NOT NULL,
  ZIP_CODE      TEXT                    NOT NULL
)