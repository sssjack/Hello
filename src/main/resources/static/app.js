const state = {
    currentQuestion: null,
    recognition: null,
    recognizing: false,
};

const els = {
    typeSelect: document.querySelector('#typeSelect'),
    yearSelect: document.querySelector('#yearSelect'),
    randomBtn: document.querySelector('#randomBtn'),
    questionHint: document.querySelector('#questionHint'),
    questionMeta: document.querySelector('#questionMeta'),
    questionContent: document.querySelector('#questionContent'),
    answerInput: document.querySelector('#answerInput'),
    wordCount: document.querySelector('#wordCount'),
    evaluateBtn: document.querySelector('#evaluateBtn'),
    startVoiceBtn: document.querySelector('#startVoiceBtn'),
    stopVoiceBtn: document.querySelector('#stopVoiceBtn'),
    voiceStatus: document.querySelector('#voiceStatus'),
    loading: document.querySelector('#loading'),
    result: document.querySelector('#result'),
};

async function init() {
    await loadYears();
    bindEvents();
    setupSpeechRecognition();
}

async function loadYears() {
    try {
        const years = await fetchJson('/api/questions/years');
        years.forEach((year) => {
            const option = document.createElement('option');
            option.value = year;
            option.textContent = `${year} 年`;
            els.yearSelect.appendChild(option);
        });
    } catch (error) {
        els.questionHint.textContent = `年份加载失败：${error.message}`;
    }
}

function bindEvents() {
    els.randomBtn.addEventListener('click', randomQuestion);
    els.answerInput.addEventListener('input', () => {
        els.wordCount.textContent = `${els.answerInput.value.trim().length} 字`;
        updateEvaluateButton();
    });
    els.evaluateBtn.addEventListener('click', evaluateAnswer);
    els.startVoiceBtn.addEventListener('click', startVoice);
    els.stopVoiceBtn.addEventListener('click', stopVoice);
}

async function randomQuestion() {
    const params = new URLSearchParams();
    if (els.yearSelect.value) params.set('year', els.yearSelect.value);
    if (els.typeSelect.value) params.set('type', els.typeSelect.value);

    els.randomBtn.disabled = true;
    els.questionHint.textContent = '正在抽题...';
    try {
        const response = await fetch(`/api/questions/random?${params.toString()}`);
        if (response.status === 204) {
            state.currentQuestion = null;
            els.questionMeta.textContent = '没有匹配题目';
            els.questionContent.textContent = '当前筛选条件下没有题目，请更换年份或考试类型。';
            return;
        }
        if (!response.ok) throw new Error(await response.text());
        state.currentQuestion = await response.json();
        renderQuestion(state.currentQuestion);
        els.result.className = 'result-empty';
        els.result.textContent = '已抽取新题目，请作答后提交评分。';
    } catch (error) {
        els.questionHint.textContent = `抽题失败：${error.message}`;
    } finally {
        els.randomBtn.disabled = false;
        updateEvaluateButton();
    }
}

function renderQuestion(question) {
    els.questionHint.textContent = '抽题成功。你可以继续点击随机出题更换题目。';
    els.questionMeta.textContent = `${question.year} 年 · ${question.typeLabel} · ${question.province || '未注明地区'} · ${question.tags || '综合题型'} · ${question.source || '题库收录'}`;
    els.questionContent.textContent = question.content;
}

async function evaluateAnswer() {
    if (!state.currentQuestion) return;
    const answer = els.answerInput.value.trim();
    if (answer.length < 20) {
        alert('回答内容至少 20 字，建议完整作答后再评分。');
        return;
    }
    els.evaluateBtn.disabled = true;
    els.loading.classList.remove('hidden');
    try {
        const result = await fetchJson('/api/evaluations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ questionId: state.currentQuestion.id, answer }),
        });
        renderResult(result);
    } catch (error) {
        els.result.className = 'result-empty';
        els.result.textContent = `评分失败：${error.message}`;
    } finally {
        els.loading.classList.add('hidden');
        updateEvaluateButton();
    }
}

function renderResult(result) {
    els.result.className = '';
    els.result.innerHTML = `
        <div class="result-score"><strong>${escapeHtml(result.score ?? 0)}</strong><span>${escapeHtml(result.level || '暂未评级')}</span></div>
        ${listSection('优点', result.strengths)}
        ${listSection('不足', result.weaknesses)}
        ${textSection('内容建议', result.contentAdvice)}
        ${textSection('结构建议', result.structureAdvice)}
        ${textSection('表达建议', result.expressionAdvice)}
        ${listSection('解题思路', result.answerFramework)}
        ${listSection('可积累金句', result.goldenSentences)}
        ${listSection('参考答题提纲', result.sampleAnswerOutline)}
    `;
}

function textSection(title, text) {
    return `<section class="result-section"><h3>${escapeHtml(title)}</h3><div class="text-block">${escapeHtml(text || '暂无')}</div></section>`;
}

function listSection(title, items = []) {
    const normalized = Array.isArray(items) && items.length ? items : ['暂无'];
    return `<section class="result-section"><h3>${escapeHtml(title)}</h3><ul>${normalized.map((item) => `<li>${escapeHtml(item)}</li>`).join('')}</ul></section>`;
}

function setupSpeechRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
        els.startVoiceBtn.disabled = true;
        els.voiceStatus.textContent = '当前浏览器不支持 Web Speech API，可直接文字输入。';
        return;
    }
    const recognition = new SpeechRecognition();
    recognition.lang = 'zh-CN';
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.onstart = () => {
        state.recognizing = true;
        els.voiceStatus.textContent = '正在识别，请开始作答...';
        els.startVoiceBtn.disabled = true;
        els.stopVoiceBtn.disabled = false;
    };
    recognition.onerror = (event) => {
        els.voiceStatus.textContent = `语音识别异常：${event.error}`;
    };
    recognition.onend = () => {
        state.recognizing = false;
        els.startVoiceBtn.disabled = false;
        els.stopVoiceBtn.disabled = true;
        els.voiceStatus.textContent = '语音输入已停止，可继续编辑文字。';
    };
    recognition.onresult = (event) => {
        let finalText = '';
        for (let i = event.resultIndex; i < event.results.length; i += 1) {
            if (event.results[i].isFinal) {
                finalText += event.results[i][0].transcript;
            }
        }
        if (finalText) {
            els.answerInput.value = `${els.answerInput.value}${finalText}`;
            els.answerInput.dispatchEvent(new Event('input'));
        }
    };
    state.recognition = recognition;
}

function startVoice() {
    if (state.recognition && !state.recognizing) state.recognition.start();
}

function stopVoice() {
    if (state.recognition && state.recognizing) state.recognition.stop();
}

function updateEvaluateButton() {
    els.evaluateBtn.disabled = !state.currentQuestion || els.answerInput.value.trim().length < 20;
}

async function fetchJson(url, options) {
    const response = await fetch(url, options);
    if (!response.ok) {
        let message = response.statusText;
        try {
            const body = await response.json();
            message = body.message || message;
        } catch (_) {
            message = await response.text();
        }
        throw new Error(message);
    }
    return response.json();
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

init();
