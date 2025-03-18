<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Multi-Factor Authentication (MFA) OTP</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 600px;
            margin: 20px auto;
            background: #ffffff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            text-align: center;
        }
        .otp-code {
            font-size: 24px;
            font-weight: bold;
            background: #007BFF;
            color: #ffffff;
            display: inline-block;
            padding: 10px 20px;
            border-radius: 5px;
            letter-spacing: 3px;
            margin: 15px 0;
        }
        .footer {
            margin-top: 20px;
            font-size: 12px;
            color: #777;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Multi-Factor Authentication (MFA) Code</h2>
        <p>Hi ${name},</p>
        <p>Use the following One-Time Password (OTP) to complete your authentication process. This code is valid for <strong>${otp_validity_minutes} minutes</strong>.</p>

        <div class="otp-code">${otp_code}</div>

        <p><strong>Do not share this code</strong> with anyone. If you did not request this, please ignore this email.</p>

        <p class="footer">© 2025 Your Company. All rights reserved.</p>
    </div>
</body>
</html>