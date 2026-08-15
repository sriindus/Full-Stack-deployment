import { useEffect, useState, type FormEvent } from "react";
import { useAppDispatch, useAppSelector } from "../../app/hooks";
import { addGreeting, loadGreetings, removeGreeting } from "./greetingsSlice";

export default function GreetingBoard() {
  const dispatch = useAppDispatch();
  const { items, status, error } = useAppSelector((state) => state.greetings);
  const [author, setAuthor] = useState("");
  const [message, setMessage] = useState("Hello, world!");

  useEffect(() => {
    dispatch(loadGreetings());
  }, [dispatch]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!author.trim() || !message.trim()) {
      return;
    }
    dispatch(addGreeting({ author: author.trim(), message: message.trim() }));
    setAuthor("");
    setMessage("");
  }

  return (
    <section className="greeting-board" aria-label="Greetings">
      <h2>Greetings from the full stack</h2>
      <p className="subtitle">
        React + Redux Toolkit (frontend) &rarr; Spring Boot REST API (backend) &rarr; PostgreSQL (database)
      </p>

      <form onSubmit={handleSubmit} data-testid="greeting-form">
        <input
          aria-label="Author"
          placeholder="Your name"
          value={author}
          onChange={(e) => setAuthor(e.target.value)}
        />
        <input
          aria-label="Message"
          placeholder="Your message"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
        />
        <button type="submit">Post greeting</button>
      </form>

      {status === "loading" && <p role="status">Loading greetings&hellip;</p>}
      {status === "failed" && (
        <p role="alert" className="error">
          {error}
        </p>
      )}

      <ul data-testid="greeting-list">
        {items.map((greeting) => (
          <li key={greeting.id}>
            <strong>{greeting.author}:</strong> {greeting.message}
            <button
              aria-label={`Delete greeting from ${greeting.author}`}
              onClick={() => dispatch(removeGreeting(greeting.id))}
            >
              &times;
            </button>
          </li>
        ))}
      </ul>

      {status === "succeeded" && items.length === 0 && (
        <p>No greetings yet &mdash; be the first to say hello!</p>
      )}
    </section>
  );
}
