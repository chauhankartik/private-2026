const App = () => {
    return (
        <div className="container">
            <h1>ReactJS Learning</h1>
            <Ch01_JsxComponents />
            <Ch02_PropsState title="Props & State" />
            <Ch03_Effects />
            <Ch04_ContextApi />
            <Ch05_CustomHooks />
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
