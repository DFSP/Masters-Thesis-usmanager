export const isDarkMode = () => localStorage.getItem('mode') === 'dark'

export const isLightMode = () => localStorage.getItem('mode') === 'light'

export const setDarkMode = () => document.body.classList.add('dark')

export const setLightMode = () => document.body.classList.add('light')

export const toggleMode = () => isDarkMode() ? setLightMode() : setDarkMode()