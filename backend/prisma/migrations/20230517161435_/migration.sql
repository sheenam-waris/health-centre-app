/*
  Warnings:

  - You are about to alter the column `generated_at` on the `sessions` table. The data in that column could be lost. The data in that column will be cast from `VarChar(20)` to `Date`.

*/
-- AlterTable
ALTER TABLE `sessions` MODIFY `generated_at` DATE NOT NULL;
