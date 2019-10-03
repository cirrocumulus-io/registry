DELETE
FROM "user"
WHERE username != 'admin';

INSERT INTO "user"
VALUES ('3c2149ac-e838-4715-9cb0-1b3427810c49',
        'user1',
        '$2a$12$21SFFJSkRWjAeFt21v5mOe6lzDb7bvDfgcBVG66UB6/2mBYv8xOxS');

INSERT INTO image
VALUES ('8fe183ec-b45c-4877-8679-7b5fc3b9935e',
        '3c2149ac-e838-4715-9cb0-1b3427810c49',
        'user1',
        'debian',
        '2019-09-28 23:05:00 Europe/Paris');

INSERT INTO image_version
VALUES ('1453b827-49bc-4ecc-bff4-d2b70c2caff3',
        '8fe183ec-b45c-4877-8679-7b5fc3b9935e',
        '9.0');

INSERT INTO image_format
VALUES ('93f5d4e7-4181-4c5e-afb3-66de2395869d',
        '1453b827-49bc-4ecc-bff4-d2b70c2caff3',
        'qcow2',
        '/v1/user1/debian/9.0/qcow2',
        'b5ec5e13aebc7eee4b0b6f2352225a99f23dbdd4317c2cb79e786d3ebb4a1b4984fdc444ee95862f976e645f0667e64380acc4f1a77d47502097d572a42f592a',
        '2019-09-28 23:05:00 Europe/Paris');
