from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.views.generic import View
from .models import Course, Section, Parent
from .models import Student as StudentModel
from .forms import AuthenticationForm, NewUserForm, StudentForm


class ScheduledCourse(object):
    def __init__(self, name, url, sections):
        self.name = name
        self.url = url
        self.sections = sections


class Index(View):
    def get(self, request):
        return render(request, 'app/index.html')


def courses(request):
    sections = Section.objects.order_by('start_time')
    scheduled_courses = set((s.course for s in sections))
    scheduled_courses = [ScheduledCourse(c.name, c.url,
        [s for s in sections if s.course == c]) for c in Course.objects.order_by('name') if c in scheduled_courses]

    return render(request, 'app/courses.html', {'courses': scheduled_courses})


@login_required
def students(request):
    user = request.user
    if user:
        if user.is_staff:
            parents = Parent.objects.order_by('name')
        else:
            parents = Parent.objects.filter(users=user)
    else:
        parents = None

    return render(request, 'app/students.html', {'parents': parents})


def proposals(request):
    sections = Section.objects.order_by('start_time')
    return render(request, 'app/proposals.html', {'sections': sections})


def _these_students_in_other_sections(section_id, students):
    these_students_in_other_sections = {}  # section -> list of student names

    for student in students:
        other_sections_with_student = student.sections.exclude(id=section_id)

        for section_with_student in other_sections_with_student:
            student_names = these_students_in_other_sections.get(section_with_student, [])
            student_names.append(student.name)
            these_students_in_other_sections[section_with_student] = student_names

    return these_students_in_other_sections


def section(request, section_id):
    section = Section.objects.get(id=int(section_id))
    students_in_this_section = section.student_set.all().order_by('name')

    these_students_in_other_sections = _these_students_in_other_sections(section.id, students_in_this_section)

    class StudentsInSection:
        def __init__(self, section, student_names):
            self.section = section
            self.student_names = ', '.join(sorted(student_names))

    students_in_sections = [StudentsInSection(section, student_names)
        for section, student_names in these_students_in_other_sections.items() if len(student_names) > 1]
    students_in_sections.sort(key=lambda ss: ss.section.start_time)

    return render(request, 'app/section.html',
        {'section': section, 'students': students_in_this_section, 'overlaps': students_in_sections})


class Login(View):
    def get(self, request):
        form = AuthenticationForm()
        new_user_form = NewUserForm()
        return render(request, 'app/login.html', {'form': form, 'new_user_form': new_user_form})

    def post(self, request):
        if 'name' in request.POST:
            form = NewUserForm(data=request.POST)
            if form.is_valid():
                cd = form.cleaned_data
                User.objects.create_user(cd['username'], cd['email'], cd['password'])
                user = authenticate(username=cd['username'], password=cd['password'])
                login(request, user)
                code = cd.get('parent_code')
                parent = Parent.objects.get(code=code) if code else Parent(name=cd['name'], email=cd['email'])
                parent.users.add(user)
                return redirect('/app/')
            else:
                return render(request, 'app/login.html', {'form': AuthenticationForm(), 'new_user_form': form})
        else:
            form = AuthenticationForm(data=request.POST)
            if form.is_valid():
                login(request, form.get_user())
                return redirect('/app/')
            else:
                return render(request, 'app/login.html', {'form': form, 'new_user_form': NewUserForm()})


class Student(View):
    def get(self, request, id_str):
        student_id = int(id_str)
        student = StudentModel.objects.filter(id=student_id).first()
        if self._student_ok(student, request):
            return render(request, 'app/student.html', {'form': StudentForm(instance=student), 'student_id': student_id})
        else:
            return redirect('/app/')

    def post(self, request, id_str):
        student_id = int(id_str)
        student = StudentModel.objects.filter(id=student_id).first()
        if self._student_ok(student, request):
            form = StudentForm(data=request.POST, instance=student)
            if form.is_valid():
                form.save()
            else:
                return render(request, 'app/student.html', {'form': form, 'student_id': student_id})

        return redirect('/app/students')

    @staticmethod
    def _student_ok(student, request):
        return student and (request.user.is_staff or request.user in student.parent.users.all())

def logOut(request):
    logout(request)
    return redirect('/')
