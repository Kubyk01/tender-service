-- account admin with password admin@
INSERT INTO USERS (
    NAME, SURNAME, EMAIL, USERNAME, PASSWORD, CREATE_AT, USER_STATUS
) VALUES (
             'admin',
             'admin',
             'admin@gmail.com',
             'admin',
             '$2a$10$rbamTbZ0DRNtU8sW/UE5iu.AiljEMrVt3USpBeW68Btu74fOaBz4G',
             '2025-06-14 20:32:32.365803',
             'Activate'
         );

INSERT INTO user_role (ID, roles)
VALUES (
           (SELECT ID FROM USERS WHERE USERNAME = 'admin'),
           'ADMIN'
       );
