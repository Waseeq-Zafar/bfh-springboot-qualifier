INSERT INTO DEPARTMENT (DEPARTMENT_ID, DEPARTMENT_NAME) VALUES
                                                            (1, 'HR'),
                                                            (2, 'Engineering');

INSERT INTO EMPLOYEE (EMP_ID, FIRST_NAME, LAST_NAME, DOB, DEPARTMENT) VALUES
                                                                          (101, 'John', 'Doe', '1995-03-15', 2),
                                                                          (102, 'Jane', 'Smith', '1990-07-10', 1);

INSERT INTO PAYMENTS (PAYMENT_ID, EMP_ID, AMOUNT, PAYMENT_TIME) VALUES
                                                                    (1, 101, 5000, '2023-02-05 10:00:00'),
                                                                    (2, 101, 6000, '2023-03-01 09:00:00'), -- salary on 1st (ignored by WHERE in question)
                                                                    (3, 102, 7000, '2023-04-10 11:00:00');
