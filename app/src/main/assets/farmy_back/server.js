// server.js
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const odbc = require('odbc');

const app = express();
const PORT = 3000;
const cors = require('cors');
app.use(cors());


app.use(bodyParser.json());
app.use(cors());

// Access 데이터베이스 연결 설정
const dbConnectionString = 'C:\\Users\\dasom\\Desktop\\4-2\\-\\farmy_back\\farmy_DB.accdb';

// 로그인 API
app.post('/login', async (req, res) => {
    const { userID, password } = req.body;
    try {
        const connection = await odbc.connect(dbConnectionString);
        const result = await connection.query(`SELECT * FROM User WHERE UserID = '${userID}' AND Password = '${password}'`);
        await connection.close();

        if (result.length > 0) {
            res.status(200).json({ success: true, message: '로그인 성공' });
        } else {
            res.status(401).json({ success: false, message: 'ID 또는 비밀번호가 잘못되었습니다.' });
        }
    } catch (error) {
        console.log('Error:', error);
        res.status(500).json({ success: false, message: '서버 오류 발생', error });
    }
});

// 회원가입 API
app.post('/register', async (req, res) => {
    const { userID, password } = req.body;
    try {
        const connection = await odbc.connect(dbConnectionString);
        
        // 이미 존재하는 사용자 확인
        const existingUser = await connection.query(`SELECT * FROM User WHERE UserID = '${userID}'`);
        if (existingUser.length > 0) {
            res.status(400).json({ success: false, message: '이미 존재하는 사용자입니다.' });
            return;
        }

        // 새로운 사용자 추가
        await connection.query(`INSERT INTO User (UserID, Password) VALUES ('${userID}', '${password}')`);
        await connection.close();

        console.log("회원가입 성공:", userID); // 회원가입 성공 메시지
        res.status(201).json({ success: true, message: '회원가입 성공!' });
    } catch (error) {
        console.log('회원가입 오류:', error); // 오류 메시지 출력
        res.status(500).json({ success: false, message: '서버 오류 발생', error });
    }
});

app.post('/login', async (req, res) => {
    const { userID, password } = req.body;
    try {
        const connection = await odbc.connect(dbConnectionString);
        const result = await connection.query(`SELECT * FROM User WHERE UserID = '${userID}' AND Password = '${password}'`);
        await connection.close();

        if (result.length > 0) {
            console.log("로그인 성공:", userID); // 로그인 성공 메시지
            res.status(200).json({ success: true, message: '로그인 성공' });
        } else {
            res.status(401).json({ success: false, message: 'ID 또는 비밀번호가 잘못되었습니다.' });
        }
    } catch (error) {
        console.log('로그인 오류:', error); // 오류 메시지 출력
        res.status(500).json({ success: false, message: '서버 오류 발생', error });
    }
});
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
app.post('/login', async (req, res) => {
    console.log('Login request received:', req.body);
    // 로그인 로직 ...
});

app.post('/register', async (req, res) => {
    console.log('Register request received:', req.body);
    // 회원가입 로직 ...
});
console.log("DB 경로:", dbConnectionString);
