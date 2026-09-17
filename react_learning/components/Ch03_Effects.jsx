const Ch03_Effects = () => {
    const [time, setTime] = React.useState(0);

    React.useEffect(() => {
        const timer = setInterval(() => setTime(t => t + 1), 1000);
        return () => clearInterval(timer); // Cleanup on unmount
    }, []); // Empty dependency array = runs once on mount

    return (
        <div className="card">
            <h2>Chapter 3: Effects (useEffect)</h2>
            <p><code>useEffect</code> handles side effects like timers, subscriptions, or data fetching outside the render cycle.</p>
            <p>Time elapsed since mount: {time} seconds</p>
        </div>
    );
};
