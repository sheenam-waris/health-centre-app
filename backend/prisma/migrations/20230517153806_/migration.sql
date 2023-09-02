/*
  Warnings:

  - The primary key for the `sessions` table will be changed. If it partially fails, the table could be left without primary key constraint.
  - You are about to alter the column `email` on the `sessions` table. The data in that column could be lost. The data in that column will be cast from `VarChar(191)` to `VarChar(100)`.
  - A unique constraint covering the columns `[email]` on the table `sessions` will be added. If there are existing duplicate values, this will fail.
  - Added the required column `generated_at` to the `sessions` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE `sessions` DROP PRIMARY KEY,
    ADD COLUMN `generated_at` VARCHAR(20) NOT NULL,
    MODIFY `email` VARCHAR(100) NOT NULL,
    ADD PRIMARY KEY (`token`);

-- CreateIndex
CREATE UNIQUE INDEX `sessions_email_key` ON `sessions`(`email`);
