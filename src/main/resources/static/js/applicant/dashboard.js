async function loadAvailableJobs() {
    try {
        const response = await fetch('/api/jobs', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        });
        
        if (response.ok) {
            const jobs = await response.json();
            const jobsList = document.getElementById('jobsList');
            
            jobsList.innerHTML = jobs.map(job => `
                <div class="job-card">
                    <h3>${job.title}</h3>
                    <p><strong>Description:</strong> ${job.description}</p>
                    <p><strong>Requirements:</strong> ${job.requirements}</p>
                    <button onclick="applyForJob(${job.id})" class="btn btn-primary">Apply</button>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function applyForJob(jobId) {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = '.pdf,.doc,.docx';
    
    fileInput.onchange = async function() {
        const file = fileInput.files[0];
        if (!file) {
            alert('Please select a resume file');
            return;
        }

        const formData = new FormData();
        formData.append('resumeFile', file);
        formData.append('jobId', jobId);
        
        try {
            const response = await fetch('/api/applications/submit', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + localStorage.getItem('token')
                },
                body: formData
            });
            
            if (response.ok) {
                alert('Application submitted successfully!');
                // Optionally refresh the applications list
                await loadApplications();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Failed to submit application: ' + error.message);
        }
    };
    
    fileInput.click();
}

async function loadApplications() {
    try {
        const response = await fetch('/api/applications/applicant', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        });
        
        if (response.ok) {
            const applications = await response.json();
            const applicationsList = document.getElementById('applicationsList');
            
            if (applications.length === 0) {
                applicationsList.innerHTML = '<p>No applications submitted yet.</p>';
                return;
            }
            
            applicationsList.innerHTML = applications.map(app => `
                <div class="application-card">
                    <h4>${app.job.title}</h4>
                    <p><strong>Status:</strong> ${app.status}</p>
                    <p><strong>Applied:</strong> ${new Date(app.applicationDate).toLocaleDateString()}</p>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to load applications');
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadAvailableJobs();
    loadApplications();
}); 