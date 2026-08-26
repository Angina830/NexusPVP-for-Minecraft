document.addEventListener('DOMContentLoaded', () => {
    // Reveal elements on load (Hero section)
    setTimeout(() => {
        const heroElements = document.querySelectorAll('.hero .fade-in');
        heroElements.forEach((el, index) => {
            setTimeout(() => {
                el.classList.add('visible');
            }, index * 200); // Staggered delay
        });
    }, 100);

    // Scroll observer for other elements
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, {
        threshold: 0.1
    });

    const fadeElements = document.querySelectorAll('section:not(.hero) .fade-in, .feature-card, .step');
    fadeElements.forEach(el => {
        el.classList.add('fade-in'); // Add base class just in case
        observer.observe(el);
    });
});
