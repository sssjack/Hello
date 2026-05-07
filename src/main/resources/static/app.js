const TOTAL_SECONDS = 5 * 60;
const DIMENSION_NAMES = ['语言表达', '内容深入', '角度多元', '政务思维及个性亮点', '紧扣题意', '逻辑结构'];

const state = {
    currentQuestion: null,
    recognition: null,
    recognizing: false,
    timerId: null,
    answerStartedAt: null,
    answerEndedAt: null,
    remainingSeconds: TOTAL_SECONDS,
};

const els = {
    typeSelect: document.querySelector('#typeSelect'),
    yearSelect: document.querySelector('#yearSelect'),
    randomBtn: document.querySelector('#randomBtn'),
    customQuestionInput: document.querySelector('#customQuestionInput'),
    useCustomQuestionBtn: document.querySelector('#useCustomQuestionBtn'),
    questionHint: document.querySelector('#questionHint'),
    questionMeta: document.querySelector('#questionMeta'),
    questionContent: document.querySelector('#questionContent'),
    answerInput: document.querySelector('#answerInput'),
    wordCount: document.querySelector('#wordCount'),
    durationDisplay: document.querySelector('#durationDisplay'),
    evaluateBtn: document.querySelector('#evaluateBtn'),
    startAnswerBtn: document.querySelector('#startAnswerBtn'),
    timerDisplay: document.querySelector('#timerDisplay'),
    timerProgress: document.querySelector('#timerProgress'),
    timerStatus: document.querySelector('#timerStatus'),
    startVoiceBtn: document.querySelector('#startVoiceBtn'),
    stopVoiceBtn: document.querySelector('#stopVoiceBtn'),
    voiceStatus: document.querySelector('#voiceStatus'),
    loading: document.querySelector('#loading'),
    result: document.querySelector('#result'),
};

async function init() {
    bindEvents();
    setupSpeechRecognition();
    resetTimer();
    await loadYears();
}

function bindEvents() {
    els.randomBtn.addEventListener('click', randomQuestion);
    els.useCustomQuestionBtn.addEventListener('click', useCustomQuestion);
    els.startAnswerBtn.addEventListener('click', startAnswerTimer);
    els.answerInput.addEventListener('input', () => {
        updateAnswerStats();
        updateEvaluateButton();
    });
    els.evaluateBtn.addEventListener('click', evaluateAnswer);
    els.startVoiceBtn.addEventListener('click', startVoice);
    els.stopVoiceBtn.addEventListener('click', stopVoice);
}

function useCustomQuestion() {
    const content = els.customQuestionInput.value.trim();
    if (content.length < 5) {
        alert('请先输入完整的自定义题目。');
        return;
    }

    state.currentQuestion = {
        id: null,
        custom: true,
        typeLabel: '自定义题',
        province: '自定义',
        tags: '自定义题',
        source: '用户输入',
        content,
    };
    renderQuestion(state.currentQuestion);
    resetTimer();
    els.answerInput.value = '';
    els.startAnswerBtn.disabled = false;
    els.result.className = 'result-empty';
    els.result.textContent = '已载入自定义题目。点击“开始答题”后提交评分。';
    els.questionHint.textContent = '已使用自定义题目。';
    updateAnswerStats();
    updateEvaluateButton();
}

async function loadYears() {
    try {
        const payload = await fetchJson('/api/questions/years');
        const years = Array.isArray(payload) ? payload : payload?.value;
        renderYearOptions(Array.isArray(years) ? years : []);
    } catch (error) {
        els.questionHint.textContent = `年份加载失败：${error.message}`;
    }
}

function renderYearOptions(years) {
    els.yearSelect.innerHTML = '<option value="">全部年份</option>';
    years.forEach((year) => {
        const option = document.createElement('option');
        option.value = year;
        option.textContent = `${year} 年`;
        els.yearSelect.appendChild(option);
    });
}

async function randomQuestion() {
    const params = new URLSearchParams();
    if (els.yearSelect.value) params.set('year', els.yearSelect.value);
    if (els.typeSelect.value) params.set('type', els.typeSelect.value);

    els.randomBtn.disabled = true;
    els.questionHint.textContent = '正在抽题...';
    try {
        const query = params.toString();
        const response = await fetch(`/api/questions/random${query ? `?${query}` : ''}`);
        if (response.status === 204) {
            clearQuestion('当前筛选条件下没有题目，请更换年份或考试类型。');
            return;
        }
        if (!response.ok) throw new Error(await response.text());

        state.currentQuestion = await response.json();
        renderQuestion(state.currentQuestion);
        resetTimer();
        els.answerInput.value = '';
        els.startAnswerBtn.disabled = false;
        els.result.className = 'result-empty';
        els.result.textContent = '已抽取新题目。点击“开始答题”后再提交评分。';
        updateAnswerStats();
    } catch (error) {
        els.questionHint.textContent = `抽题失败：${error.message}`;
    } finally {
        els.randomBtn.disabled = false;
        updateEvaluateButton();
    }
}

function clearQuestion(message) {
    state.currentQuestion = null;
    els.questionMeta.textContent = '没有匹配题目';
    els.questionContent.textContent = message;
    els.startAnswerBtn.disabled = true;
    resetTimer();
}

function renderQuestion(question) {
    els.questionHint.textContent = question.custom ? '自定义题目已载入。' : '抽题成功。';
    els.questionMeta.textContent = question.custom ? '自定义题 · 用户输入' : [
        question.custom ? '自定义题' : `${question.year} 年`,
        question.typeLabel || question.type,
        question.province || '未注明地区',
        question.tags || '综合题型',
        question.source || '题库收录',
    ].filter(Boolean).join(' · ');
    els.questionContent.textContent = question.content;
}

function startAnswerTimer() {
    if (!state.currentQuestion) return;
    window.clearInterval(state.timerId);
    state.answerStartedAt = Date.now();
    state.answerEndedAt = null;
    state.remainingSeconds = TOTAL_SECONDS;
    els.answerInput.value = '';
    els.startAnswerBtn.textContent = '重新开始答题';
    els.timerStatus.textContent = '倒计时进行中。';
    updateTimerDisplay();
    updateAnswerStats();
    state.timerId = window.setInterval(tickTimer, 1000);
    els.answerInput.focus();
    updateEvaluateButton();
}

function tickTimer() {
    if (!state.answerStartedAt) return;
    const elapsed = Math.floor((Date.now() - state.answerStartedAt) / 1000);
    state.remainingSeconds = Math.max(0, TOTAL_SECONDS - elapsed);
    updateTimerDisplay();
    updateAnswerStats();
    if (state.remainingSeconds === 0) {
        window.clearInterval(state.timerId);
        state.answerEndedAt = Date.now();
        els.timerStatus.textContent = '5 分钟已到，可继续补充。';
    }
}

function resetTimer() {
    window.clearInterval(state.timerId);
    state.timerId = null;
    state.answerStartedAt = null;
    state.answerEndedAt = null;
    state.remainingSeconds = TOTAL_SECONDS;
    els.timerStatus.textContent = state.currentQuestion ? '点击开始答题，系统记录用时。' : '抽题后开始计时。';
    els.startAnswerBtn.textContent = '开始答题';
    updateTimerDisplay();
    updateAnswerStats();
}

function updateTimerDisplay() {
    els.timerDisplay.textContent = formatTime(state.remainingSeconds);
    const progress = ((TOTAL_SECONDS - state.remainingSeconds) / TOTAL_SECONDS) * 100;
    els.timerProgress.style.width = `${Math.min(100, Math.max(0, progress))}%`;
    els.timerDisplay.classList.toggle('danger', state.remainingSeconds <= 30);
}

function updateAnswerStats() {
    els.wordCount.textContent = `${els.answerInput.value.trim().length} 字`;
    els.durationDisplay.textContent = `用时 ${formatTime(getElapsedSeconds())}`;
}

async function evaluateAnswer() {
    if (!state.currentQuestion) return;
    if (!state.answerStartedAt) {
        alert('请先点击“开始答题”，系统需要记录本题作答用时。');
        return;
    }

    const answer = els.answerInput.value.trim();
    if (answer.length < 20) {
        alert('回答内容至少 20 字。');
        return;
    }

    const answerDurationSeconds = getElapsedSeconds();
    const payload = {
        questionId: state.currentQuestion.custom ? null : state.currentQuestion.id,
        customQuestion: state.currentQuestion.custom ? state.currentQuestion.content : null,
        answer,
        answerDurationSeconds,
    };
    els.evaluateBtn.disabled = true;
    els.loading.classList.remove('hidden');
    try {
        const result = await fetchJson('/api/evaluations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json; charset=utf-8' },
            body: JSON.stringify(payload),
        });
        renderResult(result, answerDurationSeconds);
    } catch (error) {
        els.result.className = 'result-empty';
        els.result.textContent = `评分失败：${error.message}`;
    } finally {
        els.loading.classList.add('hidden');
        updateEvaluateButton();
    }
}

function renderResult(result, measuredDuration) {
    const dimensions = normalizeDimensions(result.dimensionScores);
    const duration = result.answerDurationSeconds ?? measuredDuration ?? 0;
    els.result.className = '';
    els.result.innerHTML = `
        <div class="report-hero">
            <div class="score-ring"><strong>${escapeHtml(result.score ?? 0)}</strong><span>总分 / 100</span></div>
            <div class="report-summary">
                <div class="pill-row">
                    <span class="pill">${escapeHtml(result.level || '暂未评级')}</span>
                    <span class="pill">${escapeHtml(result.questionType || '题型待判断')}</span>
                    <span class="pill">作答 ${escapeHtml(formatDurationText(duration))}</span>
                </div>
                <p>${escapeHtml(result.scoreExplanation || '暂无总评')}</p>
                <p class="muted-line">${escapeHtml(result.durationComment || '')}</p>
            </div>
        </div>
        <div class="radar-grid">
            <div class="radar-wrap">${renderRadar(dimensions)}</div>
            <div class="dimension-list">${dimensions.map(renderDimension).join('')}</div>
        </div>
        ${textSection('一、题型判断', result.questionTypeReason)}
        ${textSection('二、能否拉开分差', result.scoreGapAssessment)}
        ${listSection('三、主要扣分点', result.majorDeductions)}
        ${listSection('四、考官能听出的亮点', result.examinerHighlights)}
        ${listSection('五、面试官逐项评价', result.examinerPerspective)}
        ${listSection('六、逐句或逐段问题', result.sentenceLevelProblems)}
        ${listSection('七、可保留和强化的亮点', result.strengths)}
        ${listSection('八、优先级改进方向', result.priorityImprovements)}
        ${textSection('九、内容改进建议', result.contentAdvice)}
        ${textSection('十、结构改进建议', result.structureAdvice)}
        ${textSection('十一、表达改进建议', result.expressionAdvice)}
        ${listSection('十二、解题思路', result.answerFramework)}
        ${listSection('十三、金句或机关话术', result.goldenSentences)}
        ${listSection('十四、保留你风格后的高分改写', result.optimizedAnswer, 'ordered-text')}
        ${listSection('十五、高分示例回答', result.sampleAnswer, 'ordered-text')}
        ${listSection('十六、关键词背诵提纲', result.memorizationOutline)}
        ${listSection('十七、考场表达建议', result.deliveryAdvice)}
        ${listSection('十八、横向拓展', result.transferableScenarios)}
    `;
}

function renderDimension(item) {
    return `<div class="dimension-item"><span>${escapeHtml(item.name)}</span><strong>${escapeHtml(item.score)}/10</strong><p>${escapeHtml(item.comment || '暂无评价')}</p></div>`;
}

function renderRadar(dimensions) {
    const size = 260;
    const center = size / 2;
    const radius = 92;
    const points = dimensions.map((item, index) => pointFor(index, dimensions.length, radius * (item.score / 10), center)).join(' ');
    const rings = [0.25, 0.5, 0.75, 1]
        .map((scale) => `<polygon points="${dimensions.map((_, index) => pointFor(index, dimensions.length, radius * scale, center)).join(' ')}" />`)
        .join('');
    const axes = dimensions.map((item, index) => {
        const edge = pointFor(index, dimensions.length, radius, center).split(',');
        const label = pointFor(index, dimensions.length, radius + 25, center).split(',');
        return `<line x1="${center}" y1="${center}" x2="${edge[0]}" y2="${edge[1]}" /><text x="${label[0]}" y="${label[1]}">${escapeHtml(shortName(item.name))}</text>`;
    }).join('');
    return `<svg viewBox="0 0 ${size} ${size}" class="radar" role="img" aria-label="六维评分雷达图">
        <g class="radar-rings">${rings}</g>
        <g class="radar-axes">${axes}</g>
        <polygon class="radar-area" points="${points}" />
        <polyline class="radar-line" points="${points} ${points.split(' ')[0]}" />
    </svg>`;
}

function pointFor(index, total, radius, center) {
    const angle = -Math.PI / 2 + (2 * Math.PI * index) / total;
    return `${(center + radius * Math.cos(angle)).toFixed(1)},${(center + radius * Math.sin(angle)).toFixed(1)}`;
}

function shortName(name) {
    return name === '政务思维及个性亮点' ? '政务亮点' : name;
}

function normalizeDimensions(dimensions = []) {
    return DIMENSION_NAMES.map((name) => {
        const found = Array.isArray(dimensions) ? dimensions.find((item) => item.name === name) : null;
        return {
            name,
            score: Math.max(0, Math.min(10, Number(found?.score ?? 0))),
            comment: found?.comment || '暂无评价',
        };
    });
}

function textSection(title, text) {
    return `<section class="result-section"><h3>${escapeHtml(title)}</h3><div class="text-block">${escapeHtml(text || '暂无')}</div></section>`;
}

function listSection(title, items = [], extraClass = '') {
    const normalized = Array.isArray(items) && items.length ? items : ['暂无'];
    return `<section class="result-section ${extraClass}"><h3>${escapeHtml(title)}</h3><ul>${normalized.map((item) => `<li>${escapeHtml(item)}</li>`).join('')}</ul></section>`;
}

function setupSpeechRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
        els.startVoiceBtn.disabled = true;
        els.voiceStatus.textContent = '当前浏览器不支持语音识别，可直接文字输入。';
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
        els.voiceStatus.textContent = '语音输入已停止。';
    };
    recognition.onresult = (event) => {
        let finalText = '';
        for (let i = event.resultIndex; i < event.results.length; i += 1) {
            if (event.results[i].isFinal) finalText += event.results[i][0].transcript;
        }
        if (finalText) {
            if (!state.answerStartedAt && state.currentQuestion) startAnswerTimer();
            els.answerInput.value = `${els.answerInput.value}${finalText}`;
            els.answerInput.dispatchEvent(new Event('input'));
        }
    };
    state.recognition = recognition;
}

function startVoice() {
    if (!state.currentQuestion) {
        alert('请先抽取一道题目或输入自定义题目。');
        return;
    }
    if (!state.answerStartedAt) startAnswerTimer();
    if (state.recognition && !state.recognizing) state.recognition.start();
}

function stopVoice() {
    if (state.recognition && state.recognizing) state.recognition.stop();
}

function updateEvaluateButton() {
    els.evaluateBtn.disabled = !state.currentQuestion || !state.answerStartedAt || els.answerInput.value.trim().length < 20;
}

function getElapsedSeconds() {
    if (!state.answerStartedAt) return 0;
    const end = state.answerEndedAt ?? Date.now();
    return Math.max(0, Math.floor((end - state.answerStartedAt) / 1000));
}

function formatTime(seconds) {
    const safe = Math.max(0, Number(seconds) || 0);
    const minutes = Math.floor(safe / 60).toString().padStart(2, '0');
    const rest = Math.floor(safe % 60).toString().padStart(2, '0');
    return `${minutes}:${rest}`;
}

function formatDurationText(seconds) {
    const safe = Math.max(0, Number(seconds) || 0);
    const minutes = Math.floor(safe / 60);
    const rest = Math.floor(safe % 60).toString().padStart(2, '0');
    return `${minutes}分${rest}秒`;
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
