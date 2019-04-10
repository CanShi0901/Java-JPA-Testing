-- any sql that is to be pre-loaded before testsuite runs goes in here
-- statements must be a single line without returns
INSERT INTO ADDRESS (ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION) VALUES (1, 'Nepean', 'CA', 'K2X 5A1', 'ON', '20 Pinetrail Cress', 1)
INSERT INTO ADDRESS (ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION) VALUES (2, 'Gatineau', 'CA', 'J9A 3A3', 'QC', '430 St-Joseph Blvd', 1)
INSERT INTO EMPLOYEE (ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID) VALUES (1, 'Christine', 'Leonard', 80000, 1, 1)