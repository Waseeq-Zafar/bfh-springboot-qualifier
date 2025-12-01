CREATE TABLE DEPARTMENT (
                            DEPARTMENT_ID INT PRIMARY KEY,
                            DEPARTMENT_NAME VARCHAR(100)
);

CREATE TABLE EMPLOYEE (
                          EMP_ID INT PRIMARY KEY,
                          FIRST_NAME VARCHAR(50),
                          LAST_NAME VARCHAR(50),
                          DOB DATE,
                          DEPARTMENT INT,
                          FOREIGN KEY (DEPARTMENT) REFERENCES DEPARTMENT(DEPARTMENT_ID)
);

CREATE TABLE PAYMENTS (
                          PAYMENT_ID INT PRIMARY KEY,
                          EMP_ID INT,
                          AMOUNT DECIMAL(10,2),
                          PAYMENT_TIME TIMESTAMP,
                          FOREIGN KEY (EMP_ID) REFERENCES EMPLOYEE(EMP_ID)
);


CREATE TABLE SOLUTION (
                          ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                          REG_NO VARCHAR(255),
                          QUESTION_ID VARCHAR(255),
                          FINAL_QUERY VARCHAR(8000),
                          WEBHOOK_URL VARCHAR(255),
                          ACCESS_TOKEN_USED VARCHAR(255),
                          SUBMISSION_STATUS VARCHAR(255),
                          SUBMISSION_RESPONSE VARCHAR(8000),
                          CREATED_AT TIMESTAMP
);