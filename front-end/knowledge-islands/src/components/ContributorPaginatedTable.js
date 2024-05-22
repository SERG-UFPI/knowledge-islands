import { useState } from "react";
import { Table, Button } from "react-bootstrap";

const ContributorPaginatedTable = ({ gitRepositoryVersion }) => {
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 10;
    const totalItems = gitRepositoryVersion?.contributors.length || 0;
    const totalPages = Math.ceil(totalItems / itemsPerPage);

    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentItems = gitRepositoryVersion?.contributors.slice(startIndex, endIndex);
    const handlePreviousPage = () => {
        setCurrentPage((prevPage) => Math.max(prevPage - 1, 1));
    };

    const handleNextPage = () => {
        setCurrentPage((prevPage) => Math.min(prevPage + 1, totalPages));
    };

    return (
        <>
            <Table striped bordered hover>
                <thead>
                    <tr style={{ textAlign: "center" }}>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Active</th>
                    </tr>
                </thead>
                <tbody>
                    {currentItems?.map((contributor, id) => (
                        <tr key={id} style={{ textAlign: "center" }}>
                            <td>{contributor.name}</td>
                            <td>{contributor.email}</td>
                            <td>{contributor.active ? "Yes" : "No"}</td>
                        </tr>
                    ))}
                </tbody>
            </Table>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Button onClick={handlePreviousPage} disabled={currentPage === 1}>
                    Previous
                </Button>
                <span>Page {currentPage} of {totalPages}</span>
                <Button onClick={handleNextPage} disabled={currentPage === totalPages}>
                    Next
                </Button>
            </div>
        </>
    );
};

export default ContributorPaginatedTable;