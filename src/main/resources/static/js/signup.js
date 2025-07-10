document.getElementById('signupForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const data = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        role: document.getElementById('role').value
    };
    
    try {
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            alert('Signup successful! Please login.');
            window.location.href = '/login.html';
        } else {
            const error = await response.text();
            alert('Signup failed: ' + error);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Signup failed');
    }
}); 