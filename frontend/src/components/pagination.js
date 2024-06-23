import React from "react";
import "./pagination.css";

function Pagination ({ currentPage, setCurrentPage, totalPages }) {
  const handlePreviousPage = () => {
    setCurrentPage((prevPage) => prevPage - 1);
  };

  const handleNextPage = () => {
    setCurrentPage((prevPage) => prevPage + 1);
  };

	return (
		<div id="pagination">
			<button onClick={handlePreviousPage} disabled={currentPage === 1} className="page-buttons">
				Previous
			</button>
			{
				<button onClick={currentPage > 2 ? () => setCurrentPage(currentPage - 2) : () => {}}
				className="page-buttons">
					{currentPage > 2 ? currentPage - 2: "."}
				</button>
			}
			<button onClick={currentPage > 1 ? () => setCurrentPage(currentPage - 1) : () => {}}
				className="page-buttons">
					{currentPage > 1 ? currentPage - 1: "."}
			</button>
			<button id="current-page-button">{currentPage}</button>
			<button onClick={currentPage < totalPages ? () => setCurrentPage(currentPage + 1) : () => {}}
				className="page-buttons">
					{currentPage < totalPages ? currentPage + 1: "."}
			</button>
			<button onClick={currentPage < totalPages - 1 ? () => setCurrentPage(currentPage + 2) : () => {}}
				className="page-buttons">
					{currentPage < totalPages - 1 ? currentPage + 2: "."}
			</button>
			<button onClick={handleNextPage} disabled={currentPage === totalPages} className="page-buttons">
				Next
			</button>
		</div>
	);
};

export default Pagination;
