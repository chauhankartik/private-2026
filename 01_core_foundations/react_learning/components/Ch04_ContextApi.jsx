const ThemeContext = React.createContext();

const ThemedComponent = () => {
    const theme = React.useContext(ThemeContext);
    return <p style={{ color: theme === 'dark' ? 'var(--accent)' : '#000', fontWeight: 'bold' }}>I am consuming the "{theme}" theme from Context without receiving it as a prop!</p>;
};

const Ch04_ContextApi = () => {
    return (
        <div className="card">
            <h2>Chapter 4: Context API</h2>
            <p>Context provides a way to pass data through the component tree without having to pass props down manually at every level.</p>
            <ThemeContext.Provider value="dark">
                <ThemedComponent />
            </ThemeContext.Provider>
        </div>
    );
};
