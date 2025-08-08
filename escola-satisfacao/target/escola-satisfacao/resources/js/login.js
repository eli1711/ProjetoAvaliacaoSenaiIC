document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('loginForm');
    const togglePassword = document.querySelector('.toggle-password');
    const passwordInput = document.getElementById('senha');
    
    // Mostrar/ocultar senha
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.classList.toggle('active');
        });
    }
    
    // Validação do formulário
    if (form) {
        form.addEventListener('submit', function(e) {
            const tipoUsuario = document.getElementById('tipoUsuario').value;
            const email = document.getElementById('email').value;
            const senha = document.getElementById('senha').value;
            
            if (!tipoUsuario) {
                e.preventDefault();
                showError('Selecione o tipo de usuário');
                return;
            }
            
            if (!validateEmail(email)) {
                e.preventDefault();
                showError('Por favor, insira um e-mail válido');
                return;
            }
            
            if (senha.length < 6) {
                e.preventDefault();
                showError('A senha deve ter pelo menos 6 caracteres');
                return;
            }
        });
    }
    
    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }
    
    function showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-error';
        errorDiv.innerHTML = `<i class="alert-icon"></i> ${message}`;
        
        const existingAlert = document.querySelector('.alert');
        if (existingAlert) {
            existingAlert.replaceWith(errorDiv);
        } else {
            form.appendChild(errorDiv);
        }
    }
});