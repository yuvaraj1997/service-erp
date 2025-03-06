<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Email Verification</title>
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
        .button {
            display: inline-block;
            padding: 12px 20px;
            color: #ffffff;
            background-color: #007BFF;
            text-decoration: none;
            font-size: 16px;
            border-radius: 5px;
            margin-top: 20px;
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
        <h2>Verify Your Email</h2>
        <p>Hi ${name},</p>
        <p>Thank you for signing up! Please verify your email address by clicking the button below.</p>
        <a href="${verification_link}" class="button">Verify Email</a>
        <p>If you did not create an account, you can safely ignore this email.</p>
        <p class="footer">© 2025 Your Company. All rights reserved.</p>
    </div>
</body>
</html>
