-- CreateTable
CREATE TABLE `sessions` (
    `email` VARCHAR(191) NOT NULL,
    `token` VARCHAR(256) NOT NULL,

    UNIQUE INDEX `sessions_token_key`(`token`),
    PRIMARY KEY (`email`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
