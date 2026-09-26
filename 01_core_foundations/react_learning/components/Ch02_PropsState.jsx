const Ch02_PropsState = ({ title }) => {
    const [count, setCount] = React.useState(0);

    return (
        <div className="card">
            <h2>Chapter 2: {title}</h2>
            <p>Props are read-only data passed from parents. State is internal data that triggers re-renders when changed.</p>
            <div className="count-display">Count: {count}</div>
            <button onClick={() => setCount(count + 1)}>Increment State</button>
        </div>
    );
};
