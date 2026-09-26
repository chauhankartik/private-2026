// Custom Hook
function useToggle(initialValue = false) {
    const [value, setValue] = React.useState(initialValue);
    const toggle = () => setValue(!value);
    return [value, toggle];
}

const Ch05_CustomHooks = () => {
    const [isOn, toggleIsOn] = useToggle(false);

    return (
        <div className="card">
            <h2>Chapter 5: Custom Hooks</h2>
            <p>Custom hooks let you extract stateful component logic into reusable functions.</p>
            <p className="count-display">Status: {isOn ? "🟢 ON" : "🔴 OFF"}</p>
            <button onClick={toggleIsOn}>Toggle Switch</button>
        </div>
    );
};
