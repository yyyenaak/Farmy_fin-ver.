const express = require('express');
const fetch = require('node-fetch'); // fetch API 사용을 위해 설치 필요 (npm install node-fetch)
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json()); // JSON 미들웨어 사용

app.post('/api/generate-text', async (req, res) => {
    const prompt = req.body.prompt;
    try {
        // GPT 모델에 요청을 보내는 부분
        const response = await fetch('https://chatgpt.com/g/g-vCcW2Nq1m-farmy', { // GPT 모델 API URL
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': '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' // 필요한 경우 API 키 사용
            },
            body: JSON.stringify({ prompt: prompt, max_tokens: 150 })
        });
        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error('Error calling the GPT model:', error);
        res.status(500).json({ error: 'Error calling the GPT model' });
    }
});

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
