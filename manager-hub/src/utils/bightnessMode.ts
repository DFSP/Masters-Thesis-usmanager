export const isDarkMode = () => {
    const mode = localStorage.getItem('brightness-mode');
    return !mode || mode === 'dark-mode';
}

export const isLightMode = () => localStorage.getItem('brightness-mode') === 'light-mode';

export const setDarkMode = () => {
    document.body.classList.remove('light-mode');
    localStorage.setItem('brightness-mode', 'dark-mode');
}

export const setLightMode = () => {
    document.body.classList.add('light-mode');
    localStorage.setItem('brightness-mode', 'light-mode');
}

export const toggleBrightness = () => isDarkMode() ? setLightMode() : setDarkMode()