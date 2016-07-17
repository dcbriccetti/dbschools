from django.conf.urls import url
from . import views

urlpatterns = [
    url(r'^$',          views.Index.as_view(), name='index'),
    url(r'^login$',     views.Login.as_view(), name='login'),
    url(r'^logout$',    views.logOut),
    url(r'^courses$',   views.courses,   name='courses'),
    url(r'^student$', views.Student.as_view()),
    url(r'^student/([0-9]+)$', views.Student.as_view()),
    url(r'^section/([0-9]+)$', views.section, name='section'),
    url(r'^students$',  views.students,  name='students'),
    url(r'^proposals$', views.proposals, name='proposals'),
]
