import GreetingBoard from "./features/greetings/GreetingBoard";
import "./App.css";

function App() {
  return (
    <div className="app-shell">
      <header>
        <h1>Java Full-Stack Hello World</h1>
        <p>ReactJS + TypeScript + Redux &middot; Spring Boot &middot; Microservices &middot; PostgreSQL</p>
      </header>
      <main>
        <GreetingBoard />
      </main>
    </div>
  );
}

export default App;
