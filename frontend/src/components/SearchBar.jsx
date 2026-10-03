export default function SearchBar({ value, onChange }) {
  return (
    <input
      type="text"
      className="search-input"
      placeholder="Search tasks..."
      aria-label="Search tasks"
      maxLength={100}
      value={value}
      onChange={(e) => onChange(e.target.value)}
    />
  );
}
