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
        '9');

INSERT INTO image_format
VALUES ('93f5d4e7-4181-4c5e-afb3-66de2395869d',
        '1453b827-49bc-4ecc-bff4-d2b70c2caff3',
        'qcow2',
        '/v1/user1/debian/9/qcow2',
        '8f14ceb5224148cd03648aed62803ef9b1155062d1f685b3945f22e9298e8bdfa68d3372864b6b0dcc205e3e2da7befb439dfdd3c245ce9f20726936a612664d',
        '2019-09-28 23:05:00 Europe/Paris');
