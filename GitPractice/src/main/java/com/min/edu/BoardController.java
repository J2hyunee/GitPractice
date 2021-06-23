package com.min.edu;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.min.edu.comm.AnswerboardUtil;
import com.min.edu.dto.AnswerBoardDto;
import com.min.edu.dto.StarMemberDto;
import com.min.edu.model.AnswerBoardDaoImpl;
import com.min.edu.model.IAnswerBoardDao;
import com.min.edu.model.IStarMemberDao;
import com.min.edu.model.StarMemberDao_Impl;

public class BoardController extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8537490773088447885L;
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html; charset=UTF-8;");
		String cmd = req.getParameter("command");
		IAnswerBoardDao dao = new AnswerBoardDaoImpl();
		IStarMemberDao dao2 = new StarMemberDao_Impl();
		
		if(AnswerboardUtil.commandChk(cmd).equals("")) {
			log.info("잘못된 command 요청");
			req.setAttribute("message", "\n 잘못된 요청을 하셨습니다.\n");
			forward(req, resp, "/WEB-INF/views/error.jsp");
		} else if(AnswerboardUtil.commandChk(cmd).equals("boardList")) {
			System.out.println("전체글조회 페이지 이동");
			int page = 0;
			int count = dao.textCount();
			int maxPage = (count-1)/10 + 1;
			if(req.getParameter("page")==null) {
				page = 1;
			} else {
				page = Integer.parseInt(req.getParameter("page"));
			}
			if (page<1) {
				page = 1;
			} else if (page > maxPage) {
				page = maxPage;
			}
			System.out.println(page);
			int start = (page-1)*10;
			List<AnswerBoardDto> lists = dao.selectAllBoard(start);
			req.setAttribute("lists", lists);
			req.setAttribute("count", count);
			req.setAttribute("page", page);
			req.setAttribute("maxPage", maxPage);
			forward(req, resp, "/WEB-INF/views/boardList.jsp");
		} else if(AnswerboardUtil.commandChk(cmd).equals("detailBoard")) {
			System.out.println("상세 페이지 이동");
			String seq = req.getParameter("seq");
			System.out.println("전달 받은 parameter : " + seq);
			AnswerBoardDto dto = dao.selectDetailBoard(seq);
			req.setAttribute("dto",dto);
			forward(req, resp, "/WEB-INF/views/detailBoard.jsp");
		} else if(AnswerboardUtil.commandChk(cmd).equals("writeForm")) {
//			resp.sendRedirect("./writeForm.jsp");
			HttpSession session = req.getSession();
			System.out.println(session.getAttribute("member"));
			if(session.getAttribute("member")==null) {
				PrintWriter out = resp.getWriter();
				String str = "<script type=\"text/javascript\">" +
				"alert(\"로그인을 해주세요.\");"+
				"location.href=\"./board.do?command=signIn\";" +
				"</script>";
				out.print(str);
			} else {
				forward(req, resp, "/WEB-INF/views/writeForm.jsp");
			}
		} else if(AnswerboardUtil.commandChk(cmd).equals("modifyForm")) {
			String seq = req.getParameter("seq");
			AnswerBoardDto dto = dao.selectDetailBoard(seq);
			req.setAttribute("dto", dto);
			forward(req, resp, "/WEB-INF/views/modifyForm.jsp");
		}  else if(AnswerboardUtil.commandChk(cmd).equals("signIn")) {
			forward(req, resp, "/WEB-INF/views/signIn.jsp");
		} else if(AnswerboardUtil.commandChk(cmd).equals("logout")) {
			HttpSession session = req.getSession();
			session.removeAttribute("member");
			PrintWriter out = resp.getWriter();
			String str = "<script type=\"text/javascript\">"+
					"alert(\"로그아웃이 완료되었습니다.\");"+
					"location.href=\"./board.do?command=boardList\";"+
				"</script>";
			out.print(str);
		} else if(AnswerboardUtil.commandChk(cmd).equals("signUp")) {
			forward(req, resp, "/WEB-INF/views/signUp.jsp");
		} else if(AnswerboardUtil.commandChk(cmd).equalsIgnoreCase("idChk")) {
			String id = req.getParameter("id");
			System.out.println(id);
			StarMemberDto dto = dao2.idChk(id);
			boolean isc = true;
			if(dto != null) {
				isc = false;
			}
			forward(req, resp, "/WEB-INF/views/idChk.jsp?isc="+isc);
		} else if(AnswerboardUtil.commandChk(cmd).equals("myInfo")) {
			HttpSession session = req.getSession();
			StarMemberDto member = (StarMemberDto)session.getAttribute("member");
			StarMemberDto dto = dao2.getUser(member.getSeq());
			req.setAttribute("member", dto);
			forward(req, resp, "/WEB-INF/views/myInfo.jsp");
		} else if (AnswerboardUtil.commandChk(cmd).equals("deluser")) {
			HttpSession session = req.getSession();
			StarMemberDto dto = (StarMemberDto)session.getAttribute("member");
			boolean isc = dao2.delUser(dto.getSeq());
			PrintWriter out = resp.getWriter();
			if(isc) {
				String str = "<script type=\"text/javascript\">"+
					"alert(\"이용해 주셔서 감사합니다.\");"+
					"location.href=\"./board.do?command=logout\";"+
				"</script>";
				out.print(str);
			} else {
				String str = "<script type=\"text/javascript\">"+
					"alert(\"회원탈퇴 실패!\");"+
					"location.href=\"./board.do?command=myInfo\";"+
				"</script>";
				out.print(str);
			}
		} else if(AnswerboardUtil.commandChk(cmd).equals("updateForm")) {
			HttpSession session = req.getSession();
			StarMemberDto ldto = (StarMemberDto)session.getAttribute("member");
			if(ldto==null || ldto.getId() == null) {
				PrintWriter out = resp.getWriter();
				String str = "<script type=\"text/javascript\">"+
					"location.href=\"./controller.jsp?command=myInfo\";"+
				"</script>";
				out.print(str);
			} else {
				StarMemberDto dto = dao2.getUser(ldto.getSeq());
				req.setAttribute("member", dto);
				forward(req, resp, "/WEB-INF/views/updateForm.jsp");
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html; charset=UTF-8;");
		String cmd = req.getParameter("command");
		log.info("여기는 post입니다.");
		IAnswerBoardDao dao = new AnswerBoardDaoImpl();
		IStarMemberDao dao2 = new StarMemberDao_Impl();
		
		if(AnswerboardUtil.commandChk(cmd).equals("multiDel")) {
			String[] seq = req.getParameterValues("ch");
			System.out.println(Arrays.toString(seq));
			Map<String, String[]> map = new HashMap<String, String[]>();
			map.put("seqs", seq);
			boolean isc = dao.multiDelBoard(map);
			if(isc) {
				resp.sendRedirect("./board.do?command=boardList");
			} else {
				req.setAttribute("message", "삭제 기능에 오류가 있습니다\n관리자에게 문의하세요");
				forward(req, resp, "/WEB-INF/views/error.jsp");
			}
		} else if(AnswerboardUtil.commandChk(cmd).equals("writeValue")) {
			HttpSession session = req.getSession();
			StarMemberDto dto2 = (StarMemberDto)session.getAttribute("member");
			String id = dto2.getId();
			String title = req.getParameter("title");
			String content = req.getParameter("ir1");
			System.out.printf("%s / %s / %s",id,title,content);
			content = content.replaceAll("(\r\n|\n|\r)","<br>");
			AnswerBoardDto dto = new AnswerBoardDto(0,id,title,content);
			boolean isc = dao.insertBoard(dto);
			if(isc) {
				//pageContext.forward("./board.do?command=boardList");
				//response.sendRedirect("./board.do?command=boardList");
				resp.sendRedirect("./board.do?command=detailBoard&seq="+dto.getSeq());
			} else {
				req.setAttribute("message", "새글등록 기능에 문제가 있습니다\n관리자에게 문의하세요");
				forward(req, resp, "/WEB-INF/views/error.jsp");
			}
		} else if(AnswerboardUtil.commandChk(cmd).equals("deleteBoard")) {
			String seq = req.getParameter("seq");
			boolean isc = dao.deleteRealBoard(seq);
			if(isc) {
				resp.sendRedirect("./board.do?command=boardList");
			} else {
				req.setAttribute("message", "삭제 기능에 문제가 있습니다\n관리자에게 문의하세요");
				forward(req, resp, "/WEB-INF/views/error.jsp");
			}
		} else if(AnswerboardUtil.commandChk(cmd).equals("modifyValue")) {
			String seq = req.getParameter("seq");
			String content = req.getParameter("content");
			System.out.println(seq);
			System.out.println(content);
			content = AnswerboardUtil.conversionContent(content);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("seq",seq);
			map.put("content",content);
			boolean isc = dao.modifyBoard(map);
			if(isc) {
				resp.sendRedirect("./board.do?command=detailBoard&seq="+seq);
			} else {
				req.setAttribute("message", "수정 기능에 문제가 있습니다\n관리자에게 문의하세요");
				forward(req, resp, "/WEB-INF/views/error.jsp");
			}
		} else if(AnswerboardUtil.commandChk(cmd).equals("replyForm")) {
			String seq = req.getParameter("seq");
//			PrintWriter o = response.getWriter();
//			o.append(seq);
			HttpSession session = req.getSession();
			if(session.getAttribute("member")==null) {
				PrintWriter out = resp.getWriter();
				String str = "<script type=\"text/javascript\">" +
				"alert(\"로그인을 해주세요.\");"+
				"location.href=\"./board.do?command=signIn\";" +
				"</script>";
				out.print(str);
			} else {
				AnswerBoardDto dto = dao.selectDetailBoard(seq);
				req.setAttribute("dto", dto);
				forward(req, resp, "/WEB-INF/views/replyForm.jsp");
			}
			
		} else if(AnswerboardUtil.commandChk(cmd).equals("replyValue")) {
			String seq = req.getParameter("seq");
			HttpSession session = req.getSession();
			StarMemberDto dto2 = (StarMemberDto)session.getAttribute("member");
			String id = dto2.getId();
			String title = req.getParameter("title");
			String content = req.getParameter("content");
			content = AnswerboardUtil.conversionContent(content);
			log.info(seq);
			log.info(id);
			log.info(title);
			log.info(content);
			AnswerBoardDto dto = new AnswerBoardDto(Integer.parseInt(seq),id,title,content);
			boolean isc = dao.reply(dto);
			
			if(isc) {
				resp.sendRedirect("./board.do?command=boardList");
			} else {
				req.setAttribute("message", "답글 기능에 오류가 있습니다\n관리자에게 문의하세요");
				forward(req, resp, "/WEB-INF/views/error.jsp");
			}
		}  else if(AnswerboardUtil.commandChk(cmd).equals("login")) {
			String id = req.getParameter("id");
			String password = req.getParameter("password");
			System.out.printf("id : %s pw : %s\n",id, password);
			StarMemberDto dto = dao2.getLogin(id, password);
			System.out.println(dto);
			if(dto!=null) {
				HttpSession session = req.getSession();
				session.setAttribute("member", dto);
				resp.sendRedirect("./board.do?command=boardList");
			} else {
				PrintWriter out = resp.getWriter();
				String str = "<script type=\"text/javascript\">" +
				"alert(\"아이디와 패스워드를 확인해 주세요\");"+
				"location.href=\"./board.do?command=signIn\";" +
				"</script>";
				out.print(str);
			}
		} else if(AnswerboardUtil.commandChk(cmd).equals("adduser")) {
			String id = req.getParameter("id");
			String pw = req.getParameter("pw");
			String name = req.getParameter("name");
			String address = req.getParameter("address");
			String phone = req.getParameter("phone");
			String email = req.getParameter("email");
			StarMemberDto dto = new StarMemberDto(0,id,pw,name,address,phone,email,"","");
			System.out.println(dto);
			boolean isc = dao2.insertUser(dto);
			if(isc) {
				PrintWriter out = resp.getWriter();
				String str = "<script type=\"text/javascript\">"+
						"alert(\"회원가입이 완료되었습니다.\");"+
						"location.href=\"./board.do?command=boardList\";"+
					"</script>";
				out.print(str);
			} else {
				PrintWriter out = resp.getWriter();
				String str = "<script type=\"text/javascript\">"+
					"alert(\"회원가입  실패!\");"+
					"location.href=\"./board.do?command=signUp\";"+
				"</script>";
				out.print(str);
			}
		} else if(AnswerboardUtil.commandChk(cmd).equals("updateuser")) {
			HttpSession session = req.getSession();
			StarMemberDto ldto = (StarMemberDto)session.getAttribute("member");
			ldto.setAddress(req.getParameter("address"));
			ldto.setPhone(req.getParameter("phone"));
			ldto.setEmail(req.getParameter("email"));
			boolean isc = dao2.updateUser(ldto);
			PrintWriter out = resp.getWriter();
			if(isc) {
				String re = jsForward("회원정보 수정 완료", "./board.do?command=myInfo");
				out.print(re);
			} else {
				String re = jsForward("회원정보 수정 실패!", "./board.do?command=updateForm");
				out.print(re);
			}
		}
	}

	private void forward(HttpServletRequest req, HttpServletResponse resp, String url) throws ServletException, IOException {
		RequestDispatcher dispatcher = req.getRequestDispatcher(url);
		dispatcher.forward(req, resp);
	}
	
	public String jsForward(String msg, String url) {
		String str= "<script type='text/javascript'>" +
					"	alert('"+msg+"');" +
					"	location.href='"+url+"';" +
					"</script>";
		return str;
	}
}
