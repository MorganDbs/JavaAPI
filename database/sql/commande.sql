CREATE TABLE commande (
    `id` VARCHAR(255),
    `nom_client` VARCHAR(255),
    `prenom_client` VARCHAR(255),
    `mail_client` VARCHAR(255),
    `token` VARCHAR(255),
    `etat` VARCHAR(255),
    `date` TIMESTAMP,
    `created_at` TIMESTAMP
);
INSERT INTO commande VALUES ('cdc54dd7-0d76-435f-ab20-268e5d1de3b1','dubois','morgan','morgan@dubois.fr','9Fq1CiLApYm4ZB6dOzmWzngqxjfczhHx6otxnMsvhQPBc5C8FxlPLsJmSr4aVTHn','livraison','2018-08-20 15:05:00:000','2018-02-04 22:30:31:000');
