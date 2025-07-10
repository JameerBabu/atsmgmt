document.getElementById('jobForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const submitButton = document.querySelector('button[type="submit"]');
    submitButton.disabled = true;
    submitButton.textContent = 'Posting...';
    
    const data = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        requirements: document.getElementById('requirements').value,
        postedDate: new Date().toISOString()
    };
    
    try {
        const response = await fetch('/api/jobs', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            alert('Job posted successfully!');
            window.location.href = '/employer/dashboard.html';
        } else {
            const error = await response.text();
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error:', error);
        alert(error.message || 'Failed to post job. Please try again.');
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = 'Post Job';
    }
});

async function uploadJobDescription() {
    const fileInput = document.getElementById('jobDescriptionFile');
    const file = fileInput.files[0];
    
    if (!file) {
        alert('Please select a file first.');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
    try {
        const response = await fetch('/api/jobs/analyzedescription', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: formData
        });
        
        if (response.ok) {
            const analysis = await response.json();
            
            // Populate form fields with extracted data
            document.getElementById('title').value = analysis.title || '';
            document.getElementById('description').value = analysis.responsibilities || '';
            document.getElementById('requirements').value = analysis.qualifications || '';
            
            alert('Job description parsed successfully! Form has been populated.');
        } else {
            const error = await response.text();
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error:', error);
        console.log(error);
        alert('Failed to parse job description: ' + error.message);
    }
}

// Check authentication
document.addEventListener('DOMContentLoaded', checkAuth); 