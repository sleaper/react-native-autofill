## Bugs

- Dont forget to have the same version of gradle and android studio

### EventListener for onFillRequest

- Lister is called twice: from useEffect and from headless js (SaveFilldata)
- The problem is when you open app after using the headless js script, headless js is still calling the listener => when is app on foreground app should use listener only in useEffect
