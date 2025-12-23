package org.example.lawngarden.domain.test

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/s")
class SessionTestController() {

    @PostMapping("/set")
    fun setSession(@RequestParam("name") name : String, @RequestParam value : String, session: HttpSession) : String {
        session.setAttribute(name, value)
        print(session.id)
        return "$name: $value"
    }

    @GetMapping("/get")
    fun getSession(@RequestParam("name") name : String, session: HttpSession) : List<Any> {
        session.toString()
        val id = session.id
        println("id : $id")
        val attribute = session.getAttribute(name)
        if(attribute == null) {
            return listOf("세션에 값이 없슴다.");
        }
        return listOf(attribute);
    }

    @PostMapping("/del")
    fun delSession(@RequestParam("name") name : String,
                   @RequestParam("id") id: String,
                   session: HttpSession): String {
        println("sessionID : $session.id")
        session.invalidate()
        return "$name: $id"
    }

    @GetMapping("/check")
    fun checkSession(request: HttpServletRequest): String {
        val session = request.getSession(false)
        if (session == null) {
            return "세션 없음(무효화됨)"
        }
        return "세션 있음: " + session.id;
    }

    @GetMapping("/chec")
    fun checSession(request: HttpServletRequest): String {
        return "hi"
    }
}
