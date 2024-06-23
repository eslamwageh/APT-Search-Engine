import "./ListItem.css";
function ResultsList({ results = [], query = ""}) {
  return (
    <div className="Gap-Container">
      <div className="Gap"/>
      <div className="Gap"/>
      <div className="Gap"/>
      <div id="Results-Container">
        {results.map((result) => (
          <Result result={result} key={result.id} query={query} />
        ))}
      </div>
    </div>
  );
}

function Result({ result, query }) {
  const queryWords = query.split(' ');
  const regex = new RegExp(`(${queryWords.join('|')})`, 'gi');
  let snippet = result.snippet.slice(0, 280);
  if (snippet.length !== 0) {
    snippet += '...';
  }

  return (
    <div className="ListItem">
      <a href={result.url}
        target="_blank"
        rel="noopener noreferrer"
        className="Result-Link">
          <h2>{result.title}</h2>
      </a>
      <p className="url">{result.url}</p>
      <p className="paragraph"
        dangerouslySetInnerHTML={{ __html: snippet.replace(regex, '<strong>$1</strong>') }}>
      </p>
    </div>
  );
}
// <p className="paragraph" dangerouslySetInnerHTML={{ __html: result.snippet.replace(/(specificWord1|specificWord2|specificWord3)/g, '<strong>$1</strong>') }}></p>
export default ResultsList;
