async function loadJobs() {
    try {
        const response = await fetch('/api/jobs/employer', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        });
        
        if (response.ok) {
            const jobs = await response.json();
            const jobsList = document.getElementById('jobsList');
            
            if (jobs.length === 0) {
                jobsList.innerHTML = '<p>No jobs posted yet.</p>';
                return;
            }
            
            jobsList.innerHTML = jobs.map(job => `
                <div class="job-card">
                    <h3>${job.title}</h3>
                    <p><strong>Description:</strong> ${job.description}</p>
                    <p><strong>Requirements:</strong> ${job.requirements}</p>
                    <p><strong>Click to share:</strong> ${job.shareableLink}</p>
                    <p><strong>Posted:</strong> ${new Date(job.postedDate).toLocaleDateString()}</p>
                    <button onclick="viewApplications(${job.id})" class="btn">View Applications</button>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to load jobs');
    }
}

async function viewApplications(jobId) {
    try {
        const response = await fetch(`/api/jobs/applications/${jobId}`, {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        });
        
        if (response.ok) {
            const applications = await response.json();
            const jobResponse = await fetch(`/api/jobs/${jobId}`, {
                headers: {
                    'Authorization': 'Bearer ' + localStorage.getItem('token')
                }
            });
            
            if (jobResponse.ok) {
                currentJob = await jobResponse.json();
                
                const modal = document.createElement('div');
                modal.className = 'modal';
                modal.innerHTML = `
                    <div class="modal-content">
                        <span class="close">&times;</span>
                        <h3>Applications for ${currentJob.title}</h3>
                        
                        <div class="filters-container">
                            <div class="search-box">
                                <input type="text" id="skillSearch" placeholder="Search by skills...">
                            </div>
                            <div class="search-box">
                                <input type="text" id="locationFilter" placeholder="Search by location">
                            </div>
                            <div class="filter-box">
                                <select id="experienceFilter">
                                    <option value="">Filter by Experience</option>
                                    <option value="0-2">0-2 years</option>
                                    <option value="2-5">2-5 years</option>
                                    <option value="5+">5+ years</option>
                                </select>
                                <select id="educationFilter">
                                    <option value="">Filter by Education</option>
                                    <option value="Bachelor">Bachelor's</option>
                                    <option value="Master">Master's</option>
                                    <option value="PhD">PhD</option>
                                </select>
                            </div>
                        </div>

                        <div id="applicationsContainer">
                            ${renderApplications(applications)}
                        </div>
                    </div>
                `;
                
                document.body.appendChild(modal);
                
                // Add search and filter functionality
                setupFilters(modal, applications);
                
                // Add modal close functionality
                const closeBtn = modal.querySelector('.close');
                closeBtn.onclick = () => modal.remove();
                window.onclick = (event) => {
                    if (event.target === modal) {
                        modal.remove();
                    }
                };
            }
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to load applications');
    }
}

function calculateMatchingScore(application, job) {
    let totalScore = 0;
    let weightCount = 0;

    // Skills matching (50% weight)
    if (application.skills && job.requirements) {
        const requiredSkills = job.requirements.toLowerCase().split(',').map(s => s.trim());
        const applicantSkills = application.skills.map(s => s.toLowerCase());
        
        const matchedSkills = requiredSkills.filter(reqSkill => 
            applicantSkills.some(appSkill => appSkill.includes(reqSkill))
        );
        
        const skillScore = (matchedSkills.length / requiredSkills.length) * 50;
        totalScore += skillScore;
        weightCount += 50;
    }

    // Experience matching (30% weight)
    if (application.yearsOfExperience && job.experienceRequired) {
        const applicantExp = parseInt(application.yearsOfExperience);
        const requiredExp = parseInt(job.experienceRequired);
        
        if (applicantExp >= requiredExp) {
            totalScore += 30;
        } else if (applicantExp >= requiredExp * 0.7) {
            totalScore += 20;
        } else if (applicantExp >= requiredExp * 0.5) {
            totalScore += 10;
        }
        weightCount += 30;
    }

    // Education matching (20% weight)
    if (application.highestEducation && job.educationRequired) {
        const educationLevels = {
            'PhD': 4,
            'Master': 3,
            'Bachelor': 2,
            'Diploma': 1
        };
        
        const applicantEdu = educationLevels[application.highestEducation] || 0;
        const requiredEdu = educationLevels[job.educationRequired] || 0;
        
        if (applicantEdu >= requiredEdu) {
            totalScore += 20;
        } else if (applicantEdu >= requiredEdu - 1) {
            totalScore += 10;
        }
        weightCount += 20;
    }

    // Calculate final score
    return weightCount > 0 ? Math.round((totalScore / weightCount) * 100) : 0;
}

function getScoreColor(score) {
    if (score >= 80) return '#4CAF50'; // Green
    if (score >= 60) return '#FFC107'; // Yellow
    if (score >= 40) return '#FF9800'; // Orange
    return '#F44336'; // Red
}

function renderApplications(applications) {
    if (applications.length === 0) {
        return '<p>No applications found.</p>';
    }
    
    return applications.map(app => {
        const matchingScore = calculateMatchingScore(app, currentJob);
        const scoreColor = getScoreColor(matchingScore);
        
        return `
        <div class="application-card">
            <div class="application-header">
                <div class="applicant-info">
                    <h4>${app.applicantName}</h4>
                    <div class="matching-score" style="background-color: ${scoreColor}">
                        ${matchingScore}% Match
                    </div>
                </div>
                <button onclick="toggleDetails(${app.id})" class="btn btn-secondary">
                    View Details
                </button>
            </div>
            <div id="details-${app.id}" class="application-details" style="display: none;">
                <div class="skills-section">
                    <div class="matched-skills">
                        <h4>Matched Skills</h4>
                        <ul>
                            ${app.skills ? app.skills.filter(skill => 
                                currentJob.requirements.toLowerCase().includes(skill.toLowerCase())
                            ).map(skill => `<li>${skill}</li>`).join('') : 'No matched skills'}
                        </ul>
                    </div>
                    <div class="missing-skills">
                        <h4>Missing Skills</h4>
                        <ul>
                            ${currentJob.requirements.split(',').filter(req => 
                                !app.skills || !app.skills.some(skill => 
                                    skill.toLowerCase().includes(req.toLowerCase().trim())
                                )
                            ).map(req => `<li>${req.trim()}</li>`).join('')}
                        </ul>
                    </div>
                </div>
                <p><strong>Experience:</strong> ${app.yearsOfExperience || 'Not specified'}</p>
                <p><strong>Location:</strong> ${app.location || 'Not specified'}</p>
                <p><strong>Education:</strong> ${app.highestEducation || 'Not specified'}</p>
                <p><strong>Applied:</strong> ${new Date(app.applicationDate).toLocaleDateString()}</p>
                <p><strong>Status:</strong> ${app.status}</p>
            </div>
        </div>
    `}).join('');
}

function toggleDetails(applicationId) {
    const detailsDiv = document.getElementById(`details-${applicationId}`);
    if (detailsDiv) {
        detailsDiv.style.display = detailsDiv.style.display === 'none' ? 'block' : 'none';
    }
}

function setupFilters(modal, applications) {
    const skillSearch = modal.querySelector('#skillSearch');
    const experienceFilter = modal.querySelector('#experienceFilter');
    const educationFilter = modal.querySelector('#educationFilter');
    const locationFilter = modal.querySelector('#locationFilter');
    const container = modal.querySelector('#applicationsContainer');

    console.log(skillSearch, educationFilter, educationFilter, locationFilter)

    function filterApplications() {
        // Split search terms by comma and trim whitespace
        const skillTerms = skillSearch.value.toLowerCase()
            .split(',')
            .map(term => term.trim())
            .filter(term => term.length > 0);  // Remove empty terms
            
        const expTerm = experienceFilter.value;
        const eduTerm = educationFilter.value;
        const locTerm = locationFilter.value;

        console.log(skillTerms, expTerm, eduTerm, locTerm)

        const filtered = applications.filter(app => {
            // Match any of the searched skills
            const skillMatch = skillTerms.length === 0 || (app.skills && 
                skillTerms.every(term => 
                    app.skills.some(skill => 
                        skill.toLowerCase().includes(term)
                    )
                ));
            
            const expMatch = !expTerm || matchExperience(app.yearsOfExperience, expTerm);
            const eduMatch = !eduTerm || app.highestEducation.includes(eduTerm);
            const locMatch = !locTerm || app.location.includes(locTerm);

            console.log(expMatch, eduMatch, locMatch)

            return skillMatch && expMatch && eduMatch && locMatch;
        });

        container.innerHTML = renderApplications(filtered);
    }

    // Update placeholder to indicate comma-separated search
    skillSearch.placeholder = "Search skills (comma-separated)...";
    
    skillSearch.addEventListener('input', filterApplications);
    experienceFilter.addEventListener('change', filterApplications);
    educationFilter.addEventListener('change', filterApplications);
}

function matchExperience(yearsStr, filterRange) {
    const years = parseInt(yearsStr);
    switch(filterRange) {
        case '0-2': return years >= 0 && years <= 2;
        case '2-5': return years > 2 && years <= 5;
        case '5+': return years > 5;
        default: return true;
    }
}

// Function to populate job select dropdown
async function populateJobSelect() {
    try {
        const response = await fetch('/api/jobs/employer', {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        });
        
        if (response.ok) {
            const jobs = await response.json();
            const jobSelect = document.getElementById('jobSelect');
            
            // Clear existing options except the first one
            jobSelect.innerHTML = '<option value="">Select a job...</option>';
            
            // Add job options
            jobs.forEach(job => {
                const option = document.createElement('option');
                option.value = job.id;
                option.textContent = job.title;
                jobSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading jobs for select:', error);
    }
}

// Function to upload resume for a specific job
async function uploadResumeForJob() {
    const jobId = document.getElementById('jobSelect').value;
    const fileInput = document.getElementById('resumeFile');
    const file = fileInput.files[0];
    
    if (!jobId) {
        alert('Please select a job first.');
        return;
    }
    
    if (!file) {
        alert('Please select a resume file.');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('jobId', jobId);
    
    try {
        const response = await fetch('/api/jobs/upload-resume', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: formData
        });
        
        if (response.ok) {
            alert('Resume uploaded successfully for the job!');
            // Clear the form
            document.getElementById('jobSelect').value = '';
            fileInput.value = '';
        } else {
            const error = await response.text();
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to upload resume: ' + error.message);
    }
}

// Load jobs when page loads
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadJobs();
    populateJobSelect();
}); 