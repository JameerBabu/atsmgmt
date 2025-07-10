document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const loginButton = document.querySelector('button[type="submit"]');
    loginButton.disabled = true;
    loginButton.textContent = 'Logging in...';
    
    const data = {
        username: document.getElementById('username').value,
        password: document.getElementById('password').value
    };
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.token) {
                localStorage.setItem('token', result.token);
                localStorage.setItem('role', result.role);
                localStorage.setItem('lastLogin', new Date().toISOString());
                
                if (result.role === 'EMPLOYER') {
                    window.location.href = '/employer/dashboard.html';
                } else if (result.role === 'APPLICANT') {
                    window.location.href = '/applicant/dashboard.html';
                }
            } else {
                throw new Error('Invalid server response');
            }
        } else {
            const error = await response.text();
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error:', error);
        alert(error.message || 'Login failed. Please try again.');
    } finally {
        loginButton.disabled = false;
        loginButton.textContent = 'Login';
    }
});

// Add this to prevent accessing login page if already logged in
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (token && role) {
        if (role === 'EMPLOYER') {
            window.location.href = '/employer/dashboard.html';
        } else if (role === 'APPLICANT') {
            window.location.href = '/applicant/dashboard.html';
        }
    }
}); 