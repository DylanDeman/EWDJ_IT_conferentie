// Form validation
(function () {
    'use strict'
    const forms = document.querySelectorAll('.needs-validation')
    Array.prototype.slice.call(forms).forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault()
                event.stopPropagation()
            }
            form.classList.add('was-validated')
        }, false)
    })
})()

// Tooltip initialization
document.addEventListener('DOMContentLoaded', function () {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    })
})

// Toast notifications
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toast-container') || createToastContainer()
    const toast = document.createElement('div')
    toast.className = `toast align-items-center text-white bg-${type} border-0`
    toast.setAttribute('role', 'alert')
    toast.setAttribute('aria-live', 'assertive')
    toast.setAttribute('aria-atomic', 'true')
    
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `
    
    toastContainer.appendChild(toast)
    const bsToast = new bootstrap.Toast(toast)
    bsToast.show()
    
    toast.addEventListener('hidden.bs.toast', function () {
        toast.remove()
    })
}

function createToastContainer() {
    const container = document.createElement('div')
    container.id = 'toast-container'
    container.className = 'toast-container position-fixed bottom-0 end-0 p-3'
    document.body.appendChild(container)
    return container
}

// Confirm dialog
function confirmAction(message) {
    return new Promise((resolve) => {
        const modal = document.createElement('div')
        modal.className = 'modal fade'
        modal.setAttribute('tabindex', '-1')
        modal.setAttribute('aria-hidden', 'true')
        
        modal.innerHTML = `
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Confirm Action</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p>${message}</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-primary" id="confirmButton">Confirm</button>
                    </div>
                </div>
            </div>
        `
        
        document.body.appendChild(modal)
        const modalInstance = new bootstrap.Modal(modal)
        
        modal.querySelector('#confirmButton').addEventListener('click', function () {
            modalInstance.hide()
            resolve(true)
        })
        
        modal.addEventListener('hidden.bs.modal', function () {
            modal.remove()
            resolve(false)
        })
        
        modalInstance.show()
    })
}

// Date formatting
function formatDate(date, format = 'yyyy-MM-dd') {
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    
    return format
        .replace('yyyy', year)
        .replace('MM', month)
        .replace('dd', day)
        .replace('HH', hours)
        .replace('mm', minutes)
}

// Currency formatting
function formatCurrency(amount, currency = 'EUR') {
    return new Intl.NumberFormat('nl-NL', {
        style: 'currency',
        currency: currency
    }).format(amount)
}

// Debounce function
function debounce(func, wait) {
    let timeout
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout)
            func(...args)
        }
        clearTimeout(timeout)
        timeout = setTimeout(later, wait)
    }
}

// Throttle function
function throttle(func, limit) {
    let inThrottle
    return function executedFunction(...args) {
        if (!inThrottle) {
            func(...args)
            inThrottle = true
            setTimeout(() => inThrottle = false, limit)
        }
    }
}

// Local storage wrapper
const storage = {
    set: function (key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value))
        } catch (e) {
            console.error('Error saving to localStorage', e)
        }
    },
    get: function (key) {
        try {
            const item = localStorage.getItem(key)
            return item ? JSON.parse(item) : null
        } catch (e) {
            console.error('Error reading from localStorage', e)
            return null
        }
    },
    remove: function (key) {
        try {
            localStorage.removeItem(key)
        } catch (e) {
            console.error('Error removing from localStorage', e)
        }
    },
    clear: function () {
        try {
            localStorage.clear()
        } catch (e) {
            console.error('Error clearing localStorage', e)
        }
    }
}

// Export functions
window.app = {
    showToast,
    confirmAction,
    formatDate,
    formatCurrency,
    debounce,
    throttle,
    storage
} 