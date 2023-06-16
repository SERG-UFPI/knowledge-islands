import { Container, Navbar, Nav, Button } from "react-bootstrap";
import logo from "../../assets/images/logo.png";
import { Link } from "react-router-dom";
import { useContext } from "react";
import AuthContext from "./AuthContext";

const Layout = ({ children }) => {
    const { user, logout } = useContext(AuthContext);
    return (
        <>
            <Navbar bg="primary" variant="dark">
                <Navbar.Brand as={Link} to="/" style={{marginLeft: "3%"}}>
                    {/* <img alt="" src={logo} width="40" height="30" className="d-inline-block align-top"/> */}
                    Knowledge Islands</Navbar.Brand>
                <Nav className="ms-auto">
                    {user && <Nav.Link>{user?.email}</Nav.Link>}
                    {!user &&(
                        <Nav.Link as={Link} to="/login">
                            Login
                        </Nav.Link>
                    )}
                </Nav>
                {user && (
                    <Button variant="outline-success" type="button" onClick={() => {logout()}}>
                        Logout
                    </Button>
                )}
            </Navbar>
            <Container>{children}</Container>
        </>
    );
};

export default Layout;