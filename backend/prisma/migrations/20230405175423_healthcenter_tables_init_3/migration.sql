-- CreateTable
CREATE TABLE `users` (
    `user_id` INTEGER NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `passwordHash` VARCHAR(256) NOT NULL,
    `gender` VARCHAR(50) NOT NULL,
    `dob` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(50) NOT NULL,
    `role` INTEGER NOT NULL,

    UNIQUE INDEX `users_email_key`(`email`),
    PRIMARY KEY (`user_id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `patient` (
    `patient_id` INTEGER NOT NULL,
    `rollno` VARCHAR(50) NOT NULL,
    `address` VARCHAR(250) NOT NULL,
    `hostel_details` VARCHAR(100) NOT NULL,

    UNIQUE INDEX `patient_rollno_key`(`rollno`),
    PRIMARY KEY (`patient_id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `appointment` (
    `app_id` INTEGER NOT NULL AUTO_INCREMENT,
    `patient_id` INTEGER NOT NULL,
    `reason` VARCHAR(1024) NOT NULL,
    `schedule_time` VARCHAR(50) NOT NULL,
    `status` VARCHAR(50) NOT NULL,
    `reporting_time` VARCHAR(50) NOT NULL,
    `treatment_name` VARCHAR(100) NOT NULL,
    `prescription_details` TEXT NOT NULL,
    `advice` TEXT NOT NULL,

    PRIMARY KEY (`app_id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `medical_certificate` (
    `mc_id` INTEGER NOT NULL AUTO_INCREMENT,
    `patient_id` INTEGER NOT NULL,
    `purpose` TEXT NOT NULL,
    `duration` VARCHAR(50) NOT NULL,
    `requested_at` VARCHAR(50) NOT NULL,
    `status` INTEGER NOT NULL,
    `approved_at` VARCHAR(50) NOT NULL,

    PRIMARY KEY (`mc_id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `appointment` ADD CONSTRAINT `appointment_patient_id_fkey` FOREIGN KEY (`patient_id`) REFERENCES `patient`(`patient_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `medical_certificate` ADD CONSTRAINT `medical_certificate_patient_id_fkey` FOREIGN KEY (`patient_id`) REFERENCES `patient`(`patient_id`) ON DELETE RESTRICT ON UPDATE CASCADE;
