import { useState, useEffect } from "react";
import { Table, Button, Card } from "react-bootstrap";
import { FaArrowUp, FaArrowDown } from "react-icons/fa";

const FileVersionPaginatedTable = ({ filesVersions }) => {
    const [sort_Score, set_Sort_Score] = useState("scores");
    const [sort_Order, set_Sort_Order] = useState("desc");

    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 10;
    const totalItems = filesVersions.length || 0;
    const totalPages = Math.ceil(totalItems / itemsPerPage);

    const [currentItems, setCurrentItems] = useState([]);

    useEffect(() => {
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        const fileSlice = filesVersions?.slice(startIndex, endIndex);
        setCurrentItems(fileSlice);
    }, [currentPage, filesVersions]);

    const handlePreviousPage = () => {
        setCurrentPage((prevPage) => Math.max(prevPage - 1, 1));
    };

    const handleNextPage = () => {
        setCurrentPage((prevPage) => Math.min(prevPage + 1, totalPages));
    };

    const sortFunction = (f) => {
        let sorted;
        if (f === "scores") {
            const sortOrder = sort_Order === "asc" ? "desc" : "asc";
            set_Sort_Order(sortOrder);
            sorted = [...currentItems].sort((a, b) => {
                const multi = sortOrder === "asc" ? 1 : -1;
                return multi * (a["totalKnowledge"] - b["totalKnowledge"]);
            });
        } else {
            sorted = currentItems;
        }
        setCurrentItems(sorted);
    };

    return (
        <>
            <Card>
                <Card.Body>
                    <Card.Title>Top 20 important files</Card.Title>
                    <Table striped bordered hover>
                        <thead>
                            <tr style={{ textAlign: "center" }}>
                                <th>Path</th>
                                <th onClick={() => sortFunction("scores")}>
                                    Importance Score{" "}
                                    {sort_Score === "scores" &&
                                        (sort_Order === "desc" ? (
                                            <FaArrowUp />
                                        ) : (
                                            <FaArrowDown />
                                        ))}
                                </th>
                                <th>Number of active authors</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentItems?.map((fileVersion, id) => (
                                <tr key={id} style={{ textAlign: "center" }}>
                                    <td>{fileVersion.file.path}</td>
                                    <td>{fileVersion.totalKnowledge.toFixed(3)}</td>
                                    <td>{fileVersion.numberActiveAuthor}</td>
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
                </Card.Body>
            </Card>
        </>
    );
};

export default FileVersionPaginatedTable;