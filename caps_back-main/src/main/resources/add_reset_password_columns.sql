-- Quick fix: Add reset password columns to user_accounts table
-- Run this in your MySQL client or command line
-- Command: mysql -u root -p pinterest < add_reset_password_columns.sql

USE pinterest;

-- Add the missing columns (ignore errors if they already exist)
ALTER TABLE user_accounts ADD COLUMN reset_otp VARCHAR(10) NULL;
ALTER TABLE user_accounts ADD COLUMN reset_otp_expires_at TIMESTAMP NULL;
