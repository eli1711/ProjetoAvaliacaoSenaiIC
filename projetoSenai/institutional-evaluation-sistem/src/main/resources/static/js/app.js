// Configuração da API
const API_BASE_URL = 'http://localhost:8080/api';
const QUESTIONS_ENDPOINT = `${API_BASE_URL}/questions`;

// Elementos do DOM
let questionsList = document.getElementById('questionsList');
let loadingIndicator = document.getElementById('loadingIndicator');
let errorMessage = document.getElementById('errorMessage');
let noQuestionsMessage = document.getElementById('noQuestionsMessage');

// Estado da aplicação
let currentFilter = 'all';
let questions = [];

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    initializeEventListeners();
    loadQuestions();
});

// Configurar event listeners
function initializeEventListeners() {
    // Filtros de tipo de questão
    document.querySelectorAll('#questionsTabs .nav-link').forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Ativar tab clicada e desativar outras
            document.querySelectorAll('#questionsTabs .nav-link').forEach(t => {
                t.classList.remove('active', 'active-tab');
            });
            tab.classList.add('active', 'active-tab');
            
            // Aplicar filtro
            currentFilter = tab.dataset.type;
            filterQuestions(currentFilter);
        });
    });
    
    // Alternar entre opções quantitativas e qualitativas
    document.querySelectorAll('input[name="questionType"]').forEach(radio => {
        radio.addEventListener('change', (e) => {
            if (e.target.value === 'QUANTITATIVE') {
                document.getElementById('quantitativeOptions').style.display = 'block';
                document.getElementById('qualitativeOptions').style.display = 'none';
            } else {
                document.getElementById('quantitativeOptions').style.display = 'none';
                document.getElementById('qualitativeOptions').style.display = 'block';
            }
        });
    });

    // Mostrar/ocultar opções de escala personalizada
    document.getElementById('customScale').addEventListener('change', () => {
        document.getElementById('customScaleOptions').style.display = 'block';
    });
    
    document.getElementById('scale5').addEventListener('change', () => {
        document.getElementById('customScaleOptions').style.display = 'none';
    });
    
    document.getElementById('scale10').addEventListener('change', () => {
        document.getElementById('customScaleOptions').style.display = 'none';
    });

    // Salvar questão
    document.getElementById('saveQuestion').addEventListener('click', saveQuestion);

    // Resetar o modal quando for fechado
    document.getElementById('questionModal').addEventListener('hidden.bs.modal', resetModal);
}

// Carregar questões da API
async function loadQuestions() {
    showLoading(true);
    hideError();
    
    try {
        const response = await fetch(QUESTIONS_ENDPOINT);
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        questions = await response.json();
        renderQuestions(questions);
        showNoQuestionsMessage(questions.length === 0);
    } catch (error) {
        showError(`Falha ao carregar questões: ${error.message}`);
    } finally {
        showLoading(false);
    }
}

// Filtrar questões por tipo
function filterQuestions(filterType) {
    let filteredQuestions;
    
    if (filterType === 'all') {
        filteredQuestions = questions;
    } else {
        filteredQuestions = questions.filter(q => q.type === filterType);
    }
    
    renderQuestions(filteredQuestions);
    showNoQuestionsMessage(filteredQuestions.length === 0);
}

// Renderizar lista de questões
function renderQuestions(questionsToRender) {
    questionsList.innerHTML = '';
    
    questionsToRender.forEach(question => {
        const badgeClass = question.type === 'QUANTITATIVE' ? 'bg-primary' : 'bg-success';
        const typeText = question.type === 'QUANTITATIVE' ? 'Quantitativa' : 'Qualitativa';
        
        let scaleInfo = '';
        if (question.type === 'QUANTITATIVE') {
            scaleInfo = `
                <div class="mt-2">
                    <small class="text-muted">Escala: ${question.minScale || 1} a ${question.maxScale || 5}</small>
                    <div class="scale-preview"></div>
                </div>
            `;
        } else {
            scaleInfo = `
                <small class="text-muted">Resposta: ${question.responseType === 'TEXT' ? 'Texto' : 'Dissertativa'}</small>
            `;
        }
        
        const questionCard = `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card question-card h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <span class="badge ${badgeClass} question-type-badge">${typeText}</span>
                            ${question.required ? '<span class="badge bg-warning question-type-badge">Obrigatória</span>' : ''}
                        </div>
                        <p class="card-text">${question.text}</p>
                        ${scaleInfo}
                    </div>
                    <div class="card-footer bg-transparent">
                        <button class="btn btn-sm btn-outline-primary edit-btn" data-id="${question.id}">
                            <i class="bi bi-pencil me-1"></i>Editar
                        </button>
                        <button class="btn btn-sm btn-outline-danger float-end delete-btn" data-id="${question.id}">
                            <i class="bi bi-trash me-1"></i>Excluir
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        questionsList.innerHTML += questionCard;
    });
    
    // Adicionar event listeners para os botões
    addQuestionActionListeners();
}

// Adicionar listeners para editar e excluir
function addQuestionActionListeners() {
    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const questionId = e.target.closest('.edit-btn').dataset.id;
            editQuestion(questionId);
        });
    });
    
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const questionId = e.target.closest('.delete-btn').dataset.id;
            deleteQuestion(questionId);
        });
    });
}

// Preparar modal para edição
async function editQuestion(id) {
    showLoading(true);
    
    try {
        const response = await fetch(`${QUESTIONS_ENDPOINT}/${id}`);
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        const question = await response.json();
        
        // Preencher formulário
        document.getElementById('modalTitle').textContent = 'Editar Questão';
        document.getElementById('questionId').value = question.id;
        document.getElementById('questionText').value = question.text;
        
        // Definir tipo de questão
        if (question.type === 'QUANTITATIVE') {
            document.getElementById('quantitativeType').checked = true;
            document.getElementById('quantitativeOptions').style.display = 'block';
            document.getElementById('qualitativeOptions').style.display = 'none';
            
            // Configurar escala
            if (question.minScale && question.maxScale) {
                document.getElementById('customScale').checked = true;
                document.getElementById('customScaleOptions').style.display = 'block';
                document.getElementById('minScale').value = question.minScale;
                document.getElementById('maxScale').value = question.maxScale;
            } else if (question.maxScale === 10) {
                document.getElementById('scale10').checked = true;
            } else {
                document.getElementById('scale5').checked = true;
            }
        } else {
            document.getElementById('qualitativeType').checked = true;
            document.getElementById('quantitativeOptions').style.display = 'none';
            document.getElementById('qualitativeOptions').style.display = 'block';
            document.getElementById('responseType').value = question.responseType;
        }
        
        document.getElementById('isRequired').checked = question.required;
        
        // Abrir modal
        const modal = new bootstrap.Modal(document.getElementById('questionModal'));
        modal.show();
        
    } catch (error) {
        showError(`Falha ao carregar questão: ${error.message}`);
    } finally {
        showLoading(false);
    }
}

// Salvar questão (criar ou atualizar)
async function saveQuestion() {
    const questionId = document.getElementById('questionId').value;
    const questionText = document.getElementById('questionText').value;
    const questionType = document.querySelector('input[name="questionType"]:checked').value;
    const isRequired = document.getElementById('isRequired').checked;
    
    if (!questionText.trim()) {
        alert('Por favor, insira o texto da questão.');
        return;
    }
    
    // Preparar dados para envio
    const questionData = {
        text: questionText,
        type: questionType,
        required: isRequired
    };
    
    // Adicionar campos específicos por tipo
    if (questionType === 'QUANTITATIVE') {
        if (document.getElementById('customScale').checked) {
            questionData.minScale = parseInt(document.getElementById('minScale').value);
            questionData.maxScale = parseInt(document.getElementById('maxScale').value);
        } else if (document.getElementById('scale10').checked) {
            questionData.minScale = 1;
            questionData.maxScale = 10;
        } else {
            questionData.minScale = 1;
            questionData.maxScale = 5;
        }
    } else {
        questionData.responseType = document.getElementById('responseType').value;
    }
    
    showLoading(true);
    
    try {
        const url = questionId ? `${QUESTIONS_ENDPOINT}/${questionId}` : QUESTIONS_ENDPOINT;
        const method = questionId ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(questionData)
        });
        
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        // Fechar modal e recarregar lista
        bootstrap.Modal.getInstance(document.getElementById('questionModal')).hide();
        await loadQuestions();
        
        alert('Questão salva com sucesso!');
        
    } catch (error) {
        showError(`Falha ao salvar questão: ${error.message}`);
    } finally {
        showLoading(false);
    }
}

// Excluir questão
async function deleteQuestion(id) {
    if (!confirm('Tem certeza que deseja excluir esta questão?')) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch(`${QUESTIONS_ENDPOINT}/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        await loadQuestions();
        alert('Questão excluída com sucesso!');
        
    } catch (error) {
        showError(`Falha ao excluir questão: ${error.message}`);
    } finally {
        showLoading(false);
    }
}

// Resetar modal
function resetModal() {
    document.getElementById('questionForm').reset();
    document.getElementById('modalTitle').textContent = 'Nova Questão';
    document.getElementById('questionId').value = '';
    document.getElementById('quantitativeOptions').style.display = 'block';
    document.getElementById('qualitativeOptions').style.display = 'none';
    document.getElementById('customScaleOptions').style.display = 'none';
}

// Funções auxiliares de UI
function showLoading(show) {
    loadingIndicator.style.display = show ? 'block' : 'none';
    questionsList.style.display = show ? 'none' : 'flex';
}

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}

function hideError() {
    errorMessage.style.display = 'none';
}

function showNoQuestionsMessage(show) {
    noQuestionsMessage.style.display = show ? 'block' : 'none';
}